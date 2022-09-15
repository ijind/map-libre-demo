LOCAL_PATH:= $(call my-dir)

ifneq ($(ENABLE_RUNTIME_PROTECT),)
  include $(CLEAR_VARS)
  LOCAL_MODULE := avr_runtime_pro
  LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuild/lib/$(TARGET_ARCH_ABI)/lib_avr_anti.a
  include $(PREBUILT_STATIC_LIBRARY)
endif

ifneq ($(ENABLE_RESOURCE_PROTECT),)
  include $(CLEAR_VARS)
  LOCAL_MODULE := avr_resource_pro
  LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuild/lib/$(TARGET_ARCH_ABI)/lib_avr_res_enc.a
  include $(PREBUILT_STATIC_LIBRARY)
endif

ifneq ($(ENABLE_RESOURCE_PROTECT) $(ENABLE_RUNTIME_PROTECT),)
  include $(CLEAR_VARS)
  LOCAL_MODULE := avr_hook
  LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuild/lib/$(TARGET_ARCH_ABI)/lib_avr_hook.a
  include $(PREBUILT_STATIC_LIBRARY)
endif

ifneq ($(ENABLE_DEXVMP_PROTECT),)
  include $(CLEAR_VARS)
  LOCAL_MODULE := avr_dexvmp_pro
  LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuild/lib/$(TARGET_ARCH_ABI)/lib_avr_dex_vmp.a
  include $(PREBUILT_STATIC_LIBRARY)
endif

# top so config
include $(CLEAR_VARS)
LOCAL_MODULE    := _sys_misc_
LOCAL_LDLIBS    := -llog -lz -latomic
ifeq ($(TARGET_ARCH_ABI), armeabi)
  LOCAL_ARM_MODE  := arm
endif

LOCAL_C_INCLUDES += $(LOCAL_PATH)/prebuild/include

ifneq ($(ENABLE_RUNTIME_PROTECT),)
  LOCAL_CFLAGS += -DRUNTIME_PROTECT_FLAGS=$(RUNTIME_PROTECT_FLAGS)
  LOCAL_CFLAGS += -DANTI_DELAY=$(ANTI_DELAY)
  LOCAL_CFLAGS += -DRUNTIME_PROTECT=1
  LOCAL_STATIC_LIBRARIES += avr_runtime_pro avr_hook
endif

ifneq ($(ENABLE_CHECK_CERT_MD5),)
  LOCAL_CFLAGS += -DCHECK_CERT_MD5
  LOCAL_CFLAGS += -DCERT_MD5=$(CERT_MD5)
  LOCAL_CFLAGS += -DRUNTIME_PROTECT=1
endif

ifneq ($(ENABLE_RESOURCE_PROTECT),)
  LOCAL_CFLAGS += -DRESOURCE_PROTECT=1
  LOCAL_STATIC_LIBRARIES += avr_resource_pro avr_hook
endif

ifneq ($(ENABLE_DEXVMP_PROTECT),)
  LOCAL_CFLAGS += -DDEXVMP_PROTECT=1
  LOCAL_STATIC_LIBRARIES += avr_dexvmp_pro
  LOCAL_LDLIBS += -lz -llog -latomic -landroid
endif

ifneq ($(ENABLE_CHECK_EXPIRE),)
  LOCAL_CFLAGS += -DCHECK_EXPIRE
  LOCAL_CFLAGS += -DEXPIRE_TIME=$(EXPIRE_TIME)
endif

ifneq ($(ENABLE_CHECK_PACKAGENAME),)
  LOCAL_CFLAGS += -DCHECK_PACKAGENAME
  LOCAL_CFLAGS += -DPACKAGENAME=$(PACKAGENAME)
endif

ifneq ($(ENABLE_ASAN),)
  LOCAL_ARM_MODE  := arm
  LOCAL_CFLAGS += -fsanitize=address -fno-omit-frame-pointer
  LOCAL_LDFLAGS += -fsanitize=address
endif

LOCAL_SRC_FILES += $(wildcard *.cpp)

LOCAL_CFLAGS+=-fstack-protector-all -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security -Wall
LOCAL_LDFLAGS=-z relro -z now

include $(BUILD_SHARED_LIBRARY)
