#ifndef __loggingwear_H__
#define __loggingwear_H__

#include <app.h>
#include <Elementary.h>
#include <system_settings.h>
#include <efl_extension.h>
#include <dlog.h>

#ifdef  LOG_TAG
#undef  LOG_TAG
#endif
#define LOG_TAG "loggingwear"

#if !defined(PACKAGE)
#define PACKAGE "org.example.loggingwear"
#endif

#endif /* __loggingwear_H__ */
