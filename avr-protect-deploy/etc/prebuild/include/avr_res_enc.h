#ifndef AVR_RESC_ENC_H
#define AVR_RESC_ENC_H

#ifdef __cplusplus
#define EXTERN extern "C"
#else
#define EXTERN extern
#endif

#define AVR_RES_ENC_VERSION      1.0.0

EXTERN int init_files_defence(const char *cfg_file_path, const char *pkg_name,
                              unsigned int sdk_value, const char *apk_path);

#endif /* AVR_RESC_ENC_H */
