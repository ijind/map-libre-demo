APP_CPPFLAGS += -fvisibility=hidden
APP_ABI := arm64-v8a armeabi-v7a x86 x86_64
APP_PLATFORM := android-21
ifneq ($(ENABLE_ASAN),)
	APP_STL:=c++_shared
else
	APP_STL:=c++_static
endif

#-g0
#APP_CPPFLAGS +=  -mllvm -sub -mllvm -fla -mllvm -bcf -mllvm -perFLA=30 -mllvm -perSUB=30 -mllvm -perBCF=60

#  '-mllvm -fla', '-mllvm -perFLA=20' for Control Flow Flattening, probability of 20% on each function
#  '-mllvm -sub', '-mllvm -perSUB=20' for Instructions Substitution, probability of 20% on each function
#  '-mllvm -bcf', '-mllvm -perBCF=60' for Bogus Control Flow, applies it on all functions with a probability of 60%
