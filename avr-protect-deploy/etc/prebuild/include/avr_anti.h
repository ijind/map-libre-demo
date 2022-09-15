//
// Created by blue on 2020/05/03.
//

#ifndef ANTI_H
#define ANTI_H

#include <jni.h>
#include <stdint.h>
#include <string>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

#define Anti_monitor_init                                      oOoooOoOoooOoO00O0oO
#define Anti_monitor_start                                     Oooo000OooooOO0oOOO0
#define Anti_monitor_check                                     o00oO0ooOoOo0ooOOO0o

#define Anti_detect_debug_trace_entry                          O0o0oO00OoOO00o0oo00
#define Anti_detect_debug_feature_entry                        OOoo0oo0O0OoOoo0OO00
#define Anti_detect_emulator_entry                             ooo000oOoo0OooO0o0O0
#define Anti_detect_flaw_janus_entry                           OOOOOOOo00OOooOO0O0o
#define Anti_detect_inject_entry                               O0o0O00O00o0oOO0oOO0
#define Anti_detect_memdump_entry                              OOOO0ooo0o0o0oo00o0o
#define Anti_detect_multirun_entry                             ooo0o0ooO000OOo0oo0o
#define Anti_detect_root_entry                                 O0O0oO0oooOoOoO00o0o
#define Anti_detect_thread_entry                               ooO00ooo0O0o0000oO00
#define Anti_detect_log_entry                                  O0oO0oooO00oO0OOOo0O
#define Anti_detect_virtual_app_entry                          ooooOoo00OooO0ooooO0

#define Anti_verify_pkg                                        o0ooO00OOo0o0O00OO00
#define Anti_verify_signature                                  oOoOooo0oOOO0o0O0O0O

#define Anti_set_monitor_callback                              o000OOoOoo0000o0ooO0
#define Anti_ResultCallback                                    OO0oOo0oOO00oOOoooo0

/**
 * 守护进程的反调试检测
 */
#define ANTI_FLAG_DEBUG_TRACE       (1 << 0)
#define ANTI_FLAG_DEBUG_FEATURE     (1 << 1)
#define ANTI_FLAG_EMULATOR          (1 << 2)
#define ANTI_FLAG_FLAW_JANUS        (1 << 3)
#define ANTI_FLAG_INJECT            (1 << 4)        // 0x10
#define ANTI_FLAG_MEMDUMP           (1 << 5)
#define ANTI_FLAG_MULTIRUN          (1 << 6)
#define ANTI_FLAG_VIRTUAL_APP       (1 << 7)
#define ANTI_FLAG_ROOT              (1 << 8)
#define ANTI_FLAG_THREAD            (1 << 9)
#define ANTI_FLAG_LOG               (1 << 10)

/**
 * jni 环境初始化
 * 注意：Anti功能使用前必须调用
 * @param vm
 * @param jContext jni Context实例
 */
void Anti_monitor_init(JavaVM *vm, jobject jContext);

/**
 *
 * Anti对抗功能启动，线程/进程不断检测，检测到随机不同崩溃
 * -1 默认为 ALL
 *
 */
void Anti_monitor_start(int64_t flag);

/**
 * 插装检测Anti功能
 */
bool Anti_monitor_check();

/**
 * 调试检测，守护进程
 * @return
 */
void Anti_detect_debug_trace_entry();

/**
 * 调试检测，单进程
 * @return
 */
void Anti_detect_debug_feature_entry();

/**
 * 调试模拟器
 * @return
 */
void Anti_detect_emulator_entry();

/**
 * apk 非法检测
 * @return
 */
void Anti_detect_flaw_janus_entry();

/**
 * hook注入检测
 * @return
 */
void Anti_detect_inject_entry();

/**
 * 内存 dump 检测
 * @return
 */
void Anti_detect_memdump_entry();

/**
 * 多开检测
 * @return
 */
void Anti_detect_multirun_entry();

/**
 * root 检测
 * @return
 */
void Anti_detect_root_entry();

/**
 * 监控线程
 * @return
 */
void Anti_detect_thread_entry();

/**
 * 清除日志
 * @return
 */
void Anti_detect_log_entry();

/**
 * 检测virtual app
 * @return
 */
void Anti_detect_virtual_app_entry();

/**
 * 包名检验
 * @param pkgVec
 * @return
 */
bool Anti_verify_pkg(const string &pkg);

/**
 * 签名检验
 * @param sigVec
 * @return
 */
bool Anti_verify_signature(const string &sign);

/**
 * 开启anti结果返回功能的宏
 */
#ifdef ANTI_RETURN_ON
typedef void (*Anti_ResultCallback)(int64_t flag, void *args);
void Anti_set_monitor_callback(Anti_ResultCallback callback);
#endif

#ifdef __cplusplus
}
#endif

#endif //ANTI_H
