#include "common.h"
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

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{

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


#ifdef RUNTIME_PROTECT_FLAGS
	int flag = RUNTIME_PROTECT_FLAGS;
	if (flag) {
		//传0代表由anti自动尝试获取anti，由于sdk so加载时机无法保证有context，所有只是尝试，不保证一定成功
		Anti_monitor_init(vm, 0);
		Anti_monitor_start(flag);
	}
#endif
	return JNI_VERSION_1_6;
}


void JNI_OnUnload(JavaVM *vm, void *reserved)
{
#ifdef DEXVMP_PROTECT

	bool ret = dex_vmp_exit_engine(NULL);
	if (!ret) {
		__android_log_print(ANDROID_LOG_INFO, "VMP", "exit engine failed");
	}

#endif
	return;
}
