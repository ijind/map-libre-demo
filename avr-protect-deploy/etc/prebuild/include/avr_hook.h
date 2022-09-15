#ifndef avr_hook_h
#define avr_hook_h

#include <stdbool.h>

#define AVR_HOOK_VERSION                 1.0.0

#define avr_hook_hookfun                 O0o00OOO0o0O0O0OOooo
#define avr_hook_debug                   ooo00OooOoOOoO00OOOo

typedef enum _AvrRetStatus {
    AVR_UNKOWN = -1,
    AVR_DONE = 0,
    AVR_SUCCESS,
    AVR_FAILED,
    AVR_DONE_HOOK,
    AVR_DONE_INIT,
    AVR_DONE_ENABLE,
    AVR_ALREADY_HOOK,
    AVR_ALREADY_INIT,
    AVR_ALREADY_ENABLED,
    AVR_NEED_INIT,
    AVR_NO_BUILD_HOOK,
    AVR_ABI_NOT_MATCH = 0x1000,
    AVR_SIZE_NOT_ENOUGH
} AvrRetStatus;

#ifdef __cplusplus
extern "C" {
#endif

/********************************************************************************
* NAME: 
    avr_hook_fun

* DESCRIPTION
    This function is used for install hook.

* PARAMS:
    param[IN] symbol
        This is the source function address. It will jump to the replace function after calls avr_hook_fun function.
    param[IN] replace: 
        This is the replace replace address. After calling avr_hook_fun function, replace function will be called when someone calls the source function.
        used for replace 
    param[OUT] origin:
        This is a variable used for saving orignal function address. After calling avr_hook_fun function, you can call the original function by the origin variable.

* RETURN VALUES
    The avr_hook_fun function return AVR_DONE or AVR_SUCCESS on success or others on failure.
 *******************************************************************************/
AvrRetStatus avr_hook_hookfun(void *symbol, void *replace, void **origin);

/********************************************************************************
* NAME: 
    avr_hook_fun

* DESCRIPTION
    This function is used for printing the debug log.

* PARAMS:
    param[IN] is_debug
        When is_debug is true, print the debug log; Otherwise, no printing.
 *******************************************************************************/
void avr_hook_debug(bool is_debug);

/********************************************************************************
* NAME: 
    avr_hook_make_as_system_caller

* DESCRIPTION
    This function must be called before [avr_hook_hookfun], when the lib is not called by apk.
 *******************************************************************************/
void avr_hook_make_as_system_caller(void);

#ifdef __cplusplus
}
#endif

#endif // avr_hook_h
