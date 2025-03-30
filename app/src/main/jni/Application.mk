
# Uncomment this if you're using STL in your project
# See CPLUSPLUS-SUPPORT.html in the NDK documentation for more information
# APP_STL := stlport_static 

#APP_ABI := armeabi armeabi-v7a x86
APP_ABI:= armeabi-v7a arm64-v8a x86 mips
APP_STL := c++_shared
APP_CPPFLAGS += -std=c++11