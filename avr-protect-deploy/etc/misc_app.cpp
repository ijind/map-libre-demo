#include "common.h"
#include <avr_res_enc.h>
#include <avr_anti.h>
#include <avr_dex_vmp.h>
#include <android/log.h>

#include <time.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <stdlib.h>
#include <pthread.h>
#include <inttypes.h>

#if defined(__ANDROID__)

__attribute__((constructor)) static void __my_check_uid() {

	//不给root和shell调用
	uid_t uid = getuid();
	//system app also skip
	if (uid < 10000 && uid!=1000) {

		__android_log_print(ANDROID_LOG_INFO, "AARR", "call by unexcepted user %d", (int)uid);
		exit(-1);
	}
}
#endif

static jobject sCtx = 0;
static JavaVM *sVm = 0;

// for upx
extern "C" void avrstub() {
	//实测6000以上会导致windows的ndk崩溃
	asm ("\t.space 4096\n");
}

// res-protect
extern "C"
JNIEXPORT void JNICALL
Java_ai_security_k_a_init(JNIEnv *env, jclass type, jobject context) {

	//resp entry
	env->PushLocalFrame(20);
#ifdef RESOURCE_PROTECT
	jclass ctxClz = env->GetObjectClass(context);
	jmethodID getAppInfoId = env->GetMethodID(ctxClz, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
	jobject appInfo = env->CallObjectMethod(context, getAppInfoId);
	jclass appInfoClz = env->GetObjectClass(appInfo);
	jfieldID sourceDirId = env->GetFieldID(appInfoClz, "sourceDir", "Ljava/lang/String;");
	jstring sourceDirJs = (jstring)env->GetObjectField(appInfo, sourceDirId);
	const char *apkPath = env->GetStringUTFChars(sourceDirJs, 0);
	jfieldID pkgId = env->GetFieldID(appInfoClz, "packageName", "Ljava/lang/String;");
	jstring pkgJs = (jstring)env->GetObjectField(appInfo, pkgId);
	const char *pkgName = env->GetStringUTFChars(pkgJs, 0);

	jclass buildClass = env->FindClass("android/os/Build$VERSION");
	jfieldID versionId = env->GetStaticFieldID(buildClass, "SDK_INT", "I");

	unsigned sdk_value = (unsigned )env->GetStaticIntField(buildClass, versionId);

	jclass clsCtxImpl = env->FindClass("android/app/ContextImpl");
	env->MonitorEnter(clsCtxImpl);
	const char *varName = "sSharedPrefs";
	//android 7
	if (sdk_value >= 24) {
		varName = "sSharedPrefsCache";
	}
	if (sdk_value >= 19) {
		//need clear sp cache make sure , run hook procs
		jfieldID fid = env->GetStaticFieldID(clsCtxImpl, varName, "Landroid/util/ArrayMap;");
		env->SetStaticObjectField(clsCtxImpl, fid, 0);
	}

	int r = init_files_defence((char*)"assets/_avrsp_.cfg", (char*)pkgName,
			sdk_value, (char*)apkPath);
	env->MonitorExit(clsCtxImpl);
	__android_log_print(ANDROID_LOG_INFO, "AARR", "init_files_defence return %d", r);


	env->ReleaseStringUTFChars(pkgJs, pkgName);
	env->ReleaseStringUTFChars(sourceDirJs, apkPath);
#endif
	env->PopLocalFrame(0);
}

#ifndef PACKAGENAME
#define PACKAGENAME all
#endif

#ifndef ANTI_DELAY
#define ANTI_DELAY 0
#endif

//宏嵌套宏要做两次展开
#define WRAP2(s) #s
#define WRAP(s) WRAP2(s)

#ifndef EXPIRE_TIME
#define EXPIRE_TIME 9999-12-31_23-59-59
#endif
static void _check_expire() {

	time_t t = {0};
	time(&t);

	char strtime[255] = {0};
	struct tm *timeinfo = gmtime(&t);
	strftime(strtime, sizeof(strtime), "%Y-%m-%d_%H-%M-%S", timeinfo);
	const char *expTime = WRAP(EXPIRE_TIME);
	__android_log_print(ANDROID_LOG_INFO, "SDLog", "utc expire time %s time %s", expTime, strtime);
	if (strcmp(expTime, strtime) < 0) {
		__android_log_print(ANDROID_LOG_ERROR, "SDLog", "protect has expire in utc time %s", expTime);
		exit(-3);
	}

	return;
}

// anti
inline void _open_anti(JNIEnv *env) {

#ifdef RUNTIME_PROTECT

    if (!env->IsSameObject(sCtx, 0)) {
        jobject context = env->NewLocalRef(sCtx);
        if (context) {
            Anti_monitor_init(sVm, context);
            env->DeleteLocalRef(context);
        }
    }
#endif

#ifdef RUNTIME_PROTECT_FLAGS

	int flag = RUNTIME_PROTECT_FLAGS;
	if (flag) {
        Anti_monitor_start(flag);
	}

#endif


#ifdef CHECK_CERT_MD5

    const char *certAllow = WRAP(CERT_MD5);

    if (!Anti_verify_signature(certAllow)){
        exit(-4);
    }

#endif
}

// anti
static void *delay_exec(void *param) {

	__android_log_print(ANDROID_LOG_INFO, "ARLog", "start delay %d seconds", ANTI_DELAY);

	if (ANTI_DELAY > 0) {
		sleep(ANTI_DELAY);
	}

	__android_log_print(ANDROID_LOG_INFO, "ARLog", "process run...");
	_check_expire();
	EnvScope scope(sVm);
	JNIEnv *env = scope.get();
	_open_anti(env);
	return NULL;
}

// anti
extern "C"
JNIEXPORT void JNICALL
Java_ai_security_k_b_init(JNIEnv *env, jclass type, jobject context) {
	//anti entry
	pthread_t tid_delay_exec = 0;
	sCtx = env->NewWeakGlobalRef(context);

	pthread_create(&(tid_delay_exec), NULL, ::delay_exec, 0);
	__android_log_print(ANDROID_LOG_INFO, "ARLog", "thread id: %ld", tid_delay_exec);

	pthread_detach(tid_delay_exec);
}

//extract word,by tok
//for example com.ai.totok,com.sss.abc tok is ,
//extract com.java.dingxiang to buffer, return next offset to target
static size_t _next_word(char *buf, size_t bufSz, const char *target, const char tok) {
    const char *p = strchr(target, tok);
    size_t n = 0;
    size_t skip = 0;
    if (p) {
        n = (size_t)p-(size_t)target;
        skip = n + 1;
    }
    else {
        n = strlen(target);
        skip = n;
    }
    if (n >= bufSz - 1) {
        n = bufSz - 1;
    }
    strncpy(buf, target, n);
    buf[n] = 0;
    return skip;
}

// pkg limit
static void _check_pkgs(const char* pkgName) {

	const char *pkgNameAllow = WRAP(PACKAGENAME);
	if (strcmp(pkgNameAllow, "all") == 0){
		return;
	}

	char buf[256] = {0};
	const char *p = pkgNameAllow;

	while (*p) {
		p += _next_word(buf, sizeof(buf), p, '-');
		if (strcmp(buf, pkgName) == 0) {
			return;
		}
	}

	__android_log_print(ANDROID_LOG_ERROR, "ARLog", "protect only run packageName:%s", pkgNameAllow);
	exit(-2);
	return;
}

extern "C"
JNIEXPORT void JNICALL
Java_ai_security_k_c_init(JNIEnv *env, jclass type, jobject context) {

#ifdef CHECK_PACKAGENAME
	env->PushLocalFrame(10);
    jclass ctxClz = env->GetObjectClass(context);
    jmethodID getAppInfoId = env->GetMethodID(ctxClz, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
    jobject appInfo = env->CallObjectMethod(context, getAppInfoId);
    jclass appInfoClz = env->GetObjectClass(appInfo);
    jfieldID pkgId = env->GetFieldID(appInfoClz, "packageName", "Ljava/lang/String;");
    jstring pkgJs = (jstring)env->GetObjectField(appInfo, pkgId);
    const char *pkgName = env->GetStringUTFChars(pkgJs, 0);

    _check_pkgs(pkgName);

    env->ReleaseStringUTFChars(pkgJs, pkgName);
    env->PopLocalFrame(0);
#endif

#ifdef CHECK_EXPIRE
	_check_expire();
#endif
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	sVm = vm;

	jint result = -1;
	JNIEnv *env = NULL;

	if (vm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		return result;
	}

#ifdef DEXVMP_PROTECT

	bool ret = dex_vmp_start_engine(env);
	if (!ret) {
		__android_log_print(ANDROID_LOG_INFO, "VMP", "start engine failed");
	}

#endif

	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
#ifdef DEXVMP_PROTECT

	bool ret = dex_vmp_exit_engine(NULL);
	if (!ret) {
		__android_log_print(ANDROID_LOG_INFO, "VMP", "exit engine failed");
	}

#endif
	return;
}
