#ifndef DEX_VMP_H
#define DEX_VMP_H

#include <jni.h>

#ifdef __cplusplus
#define EXTERN extern "C"
#else
#define EXTERN extern
#endif
EXTERN bool dex_vmp_start_engine(JNIEnv *env);
EXTERN bool dex_vmp_exit_engine(JNIEnv *env);

#endif /* DEX_VMP_H */
