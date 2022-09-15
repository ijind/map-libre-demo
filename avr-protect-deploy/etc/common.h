#ifndef _COMMON_H_
#define _COMMON_H_

#include <android/log.h>
#include <jni.h>
#include <string.h>
#include <sys/types.h>
#include <stdlib.h>
#include <inttypes.h>

class EnvScope {
private:
	JavaVM *m_vm;
	JNIEnv *m_env;
	bool m_attach;
public:
	EnvScope(JavaVM *vm)
		: m_vm(vm)
		, m_attach(false) {
		jint r = m_vm->GetEnv((void**)&m_env, JNI_VERSION_1_4);
		//__android_log_print(ANDROID_LOG_INFO, "SDLog", "r=%d env=%p",r, m_env);
		if (r != JNI_OK) {
			//__android_log_print(ANDROID_LOG_INFO, "SDLog", "attach");
			m_vm->AttachCurrentThread(&m_env, 0);
			if (m_env)
				m_attach = true;
		}
	}
	JNIEnv *get() {
		return m_env;
	}

	~EnvScope() {
		if (m_attach) {
			//__android_log_print(ANDROID_LOG_INFO, "SDLog", "detach");
			m_vm->DetachCurrentThread();
		}
	}
};


#endif
