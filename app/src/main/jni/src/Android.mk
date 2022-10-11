LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := main

#SDL_PATH := ../SDL

#LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SDL_PATH)/include

# Add your application source files here...
LOCAL_SRC_FILES := ru_code22_mtrade_NativeCallsClass.cpp \
    libjpeg/jutils.cpp \
    libjpeg/jquant2.cpp \
    libjpeg/jquant1.cpp \
    libjpeg/jmemnobs.cpp \
    libjpeg/jmemmgr.cpp \
    libjpeg/jidctred.cpp \
    libjpeg/jidctint.cpp \
    libjpeg/jidctfst.cpp \
    libjpeg/jidctflt.cpp \
    libjpeg/jfdctint.cpp \
    libjpeg/jfdctfst.cpp \
    libjpeg/jfdctflt.cpp \
    libjpeg/jerror.cpp \
    libjpeg/jdtrans.cpp \
    libjpeg/jdsample.cpp \
    libjpeg/jdpostct.cpp \
    libjpeg/jdphuff.cpp \
    libjpeg/jdmerge.cpp \
    libjpeg/jdmaster.cpp \
    libjpeg/jdmarker.cpp \
    libjpeg/jdmainct.cpp \
    libjpeg/jdinput.cpp \
    libjpeg/jdhuff.cpp \
    libjpeg/jddctmgr.cpp \
    libjpeg/jdcolor.cpp \
    libjpeg/jdcoefct.cpp \
    libjpeg/jdatasrc.cpp \
    libjpeg/jdatadst.cpp \
    libjpeg/jdapistd.cpp \
    libjpeg/jctrans.cpp \
    libjpeg/jcsample.cpp \
    libjpeg/jcprepct.cpp \
    libjpeg/jcphuff.cpp \
    libjpeg/jcparam.cpp \
    libjpeg/jdapimin.cpp \
    libjpeg/jcapimin.cpp \
    libjpeg/jcapistd.cpp \
    libjpeg/jccolor.cpp \
    libjpeg/jccoefct.cpp \
    libjpeg/jcdctmgr.cpp \
    libjpeg/jcinit.cpp \
    libjpeg/jchuff.cpp \
    libjpeg/jcmainct.cpp \
    libjpeg/jcmarker.cpp \
    libjpeg/jcomapi.cpp \
    libjpeg/jcmaster.cpp


#LOCAL_SHARED_LIBRARIES := SDL2

#LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog

include $(BUILD_SHARED_LIBRARY)
