#include "loggingwear.h"
#include <bluetooth.h>
#include <sensor.h>
#include <dlog.h>
#include <device/power.h>

struct accelerometerValue {
	float x;
	float y;
	float z;
};
struct gyroscopeValue {
	float x;
	float y;
	float z;
};
struct dateAndTime {
    int year;
    int month;
    int day;
    int hour;
    int minutes;
    int seconds;
    int msec;
};
/* Server address for connecting */
char *bt_server_address = NULL;
static struct accelerometerValue acceValue;
static struct gyroscopeValue gyroValue;
static struct dateAndTime date_and_time;
int server_socket_fd = -1;
bool adapter_bonded_device_cb(bt_device_info_s *device_info, void *user_data){
    if (device_info == NULL)
        return true;
    if (!strcmp(device_info->remote_name, (char*)user_data)) {
        dlog_print(DLOG_INFO, LOG_TAG, "The server device is found in bonded device list. address(%s)",
                   device_info->remote_address);
        bt_server_address = strdup(device_info->remote_address);
    }
    /* Get information about bonded device */
    int count_of_bonded_device = 1;
    dlog_print(DLOG_INFO, LOG_TAG, "Get information about the bonded device(%d)", count_of_bonded_device);
    dlog_print(DLOG_INFO, LOG_TAG, "remote address = %s.", device_info->remote_address);
    dlog_print(DLOG_INFO, LOG_TAG, "remote name = %s.", device_info->remote_name);
    dlog_print(DLOG_INFO, LOG_TAG, "service count = %d.", device_info->service_count);
    dlog_print(DLOG_INFO, LOG_TAG, "bonded?? %d.", device_info->is_bonded);
    dlog_print(DLOG_INFO, LOG_TAG, "connected?? %d.", device_info->is_connected);
    dlog_print(DLOG_INFO, LOG_TAG, "authorized?? %d.", device_info->is_authorized);

    dlog_print(DLOG_INFO, LOG_TAG, "major_device_class %d.", device_info->bt_class.major_device_class);
    dlog_print(DLOG_INFO, LOG_TAG, "minor_device_class %d.", device_info->bt_class.minor_device_class);
    dlog_print(DLOG_INFO, LOG_TAG, "major_service_class_mask %d.", device_info->bt_class.major_service_class_mask);
    count_of_bonded_device++;

    /* Keep iterating */

    return true;
}

static void *startStreaming(void *vargp){
	char buf[20];
	char *space=" ";
	char *str=(char *) malloc(128);
	struct timeval tv;
	struct tm *tm;
	bt_error_e ret;
	char data[128];
	while(1){
		gettimeofday(&tv, NULL);
		tm = localtime(&tv.tv_sec);
		date_and_time.year = tm->tm_year + 1900;
		date_and_time.month = tm->tm_mon + 1;
		date_and_time.day = tm->tm_mday;
		date_and_time.hour = tm->tm_hour;
		date_and_time.minutes = tm->tm_min;
		date_and_time.seconds = tm->tm_sec;
		date_and_time.msec = (int) (tv.tv_usec / 1000);

		sprintf(buf, "%04d-",date_and_time.year);
		strcpy(str, buf);
		sprintf(buf, "%02d-",date_and_time.month);
		strcat(str, buf);
		sprintf(buf, "%02d;",date_and_time.day);
		strcat(str, buf);
		sprintf(buf, "%02d:",date_and_time.hour);
		strcat(str, buf);
		sprintf(buf, "%02d:",date_and_time.minutes);
		strcat(str, buf);
		sprintf(buf, "%02d.",date_and_time.seconds);
		strcat(str, buf);
		sprintf(buf, "%03d ",date_and_time.msec);
		strcat(str, buf);

		sprintf(buf, "%.6f", acceValue.x);
		strcat(str, buf);
		strcat(str, space);
		sprintf(buf, "%.6f", acceValue.y);
		strcat(str, buf);
		strcat(str, space);
		sprintf(buf, "%.6f", acceValue.z);
		strcat(str, buf);
		strcat(str, space);
		sprintf(buf, "%.6f", gyroValue.x);
		strcat(str, buf);
		strcat(str, space);
		sprintf(buf, "%.6f", gyroValue.y);
		strcat(str, buf);
		strcat(str, space);
		sprintf(buf, "%.6f", gyroValue.z);
		strcat(str, buf);

		strcpy(data,str);
		dlog_print(DLOG_INFO, LOG_TAG, "sensor value:%s",data);
		ret = bt_socket_send_data(server_socket_fd, data, sizeof(data));
		if (ret != BT_ERROR_NONE)
			dlog_print(DLOG_ERROR, LOG_TAG, "[bt_socket_send_data] failed.");
		usleep(10000);
	}
}

void
socket_connection_state_changed(int result, bt_socket_connection_state_e connection_state,
                                bt_socket_connection_s *connection, void *user_data)
{
    if (result != BT_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "[socket_connection_state_changed_cb] failed. result =%d.", result);

        return;
    }

    if (connection_state == BT_SOCKET_CONNECTED) {
        dlog_print(DLOG_INFO, LOG_TAG, "Callback: Connected.");
        if (connection != NULL) {
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: Socket of connection - %d.", connection->socket_fd);
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: Role of connection - %d.", connection->local_role);
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: Address of connection - %s.", connection->remote_address);
            /* socket_fd is used for sending data and disconnecting a device */
        	server_socket_fd = connection->socket_fd;

            pthread_t thread_id;
            pthread_create(&thread_id, NULL, startStreaming, NULL);
        } else {
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: No connection data");
        }
    } else {
        dlog_print(DLOG_INFO, LOG_TAG, "Callback: Disconnected.");
        if (connection != NULL) {
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: Socket of disconnection - %d.", connection->socket_fd);
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: Address of connection - %s.", connection->remote_address);
        } else {
            dlog_print(DLOG_INFO, LOG_TAG, "Callback: No connection data");
        }
    }
}

 static void createBluetoothConection(){

	 bt_error_e ret;
	 ret = bt_initialize();
	 if (ret != BT_ERROR_NONE) {
	     dlog_print(DLOG_ERROR, LOG_TAG, "[Bluetooth initialize] failed.");
	     return;
	 }

	 const char *remote_server_name = "HTC One M9";
	 ret = bt_adapter_foreach_bonded_device(adapter_bonded_device_cb, remote_server_name);
	 if (ret != BT_ERROR_NONE)
	     dlog_print(DLOG_ERROR, LOG_TAG, "[bt_adapter_foreach_bonded_device] failed!");

	 if (bt_server_address != NULL)
	     free(bt_server_address);

	 ret = bt_socket_set_connection_state_changed_cb(socket_connection_state_changed, NULL);
	 if (ret != BT_ERROR_NONE) {
	     dlog_print(DLOG_ERROR, LOG_TAG, "[bt_socket_set_connection_state_changed_cb] failed.");

	     return;
	 }

	 //Create 2 sockets uuid correspond to 2 smartwatch bluetooth's services.

	 const char *service_uuid="00000000-0000-1000-8000-00805F9B34FB";
//	 const char *service_uuid="00000000-0000-1000-8000-77f199fd0834";
	 const char *server_mac_address="90:E7:C4:F1:08:06";

	 ret = bt_socket_connect_rfcomm(server_mac_address, service_uuid);

	 if (ret != BT_ERROR_NONE) {
	     dlog_print(DLOG_ERROR, LOG_TAG, "[bt_socket_connect_rfcomm] failed.");

	     return;
	 } else {
	     dlog_print(DLOG_INFO, LOG_TAG, "[bt_socket_connect_rfcomm] Succeeded. bt_socket_connection_state_changed_cb will be called.");
	 }

}

typedef struct appdata {
	Evas_Object *win;
	Evas_Object *conform;
	Evas_Object *label;
} appdata_s;

static void
win_delete_request_cb(void *data, Evas_Object *obj, void *event_info)
{
	ui_app_exit();
}

static void
win_back_cb(void *data, Evas_Object *obj, void *event_info)
{
	appdata_s *ad = data;
	/* Let window go to hide state. */
	elm_win_lower(ad->win);
}

static void
create_base_gui(appdata_s *ad)
{
	/* Window */
	/* Create and initialize elm_win.
	   elm_win is mandatory to manipulate window. */
	ad->win = elm_win_util_standard_add(PACKAGE, PACKAGE);
	elm_win_autodel_set(ad->win, EINA_TRUE);

	if (elm_win_wm_rotation_supported_get(ad->win)) {
		int rots[4] = { 0, 90, 180, 270 };
		elm_win_wm_rotation_available_rotations_set(ad->win, (const int *)(&rots), 4);
	}

	evas_object_smart_callback_add(ad->win, "delete,request", win_delete_request_cb, NULL);
	eext_object_event_callback_add(ad->win, EEXT_CALLBACK_BACK, win_back_cb, ad);

	/* Conformant */
	/* Create and initialize elm_conformant.
	   elm_conformant is mandatory for base gui to have proper size
	   when indicator or virtual keypad is visible. */
	ad->conform = elm_conformant_add(ad->win);
	elm_win_indicator_mode_set(ad->win, ELM_WIN_INDICATOR_SHOW);
	elm_win_indicator_opacity_set(ad->win, ELM_WIN_INDICATOR_OPAQUE);
	evas_object_size_hint_weight_set(ad->conform, EVAS_HINT_EXPAND, EVAS_HINT_EXPAND);
	elm_win_resize_object_add(ad->win, ad->conform);
	evas_object_show(ad->conform);

	/* Label */
	/* Create an actual view of the base gui.
	   Modify this part to change the view. */
	ad->label = elm_label_add(ad->conform);
	elm_object_text_set(ad->label, "<align=center>Recording..</align>");
	evas_object_size_hint_weight_set(ad->label, EVAS_HINT_EXPAND, EVAS_HINT_EXPAND);
	elm_object_content_set(ad->conform, ad->label);

	/* Show window after base gui is set up */
	evas_object_show(ad->win);
}

static bool
app_create(void *data)
{
	/* Hook to take necessary actions before main event loop starts
		Initialize UI resources and application's data
		If this function returns true, the main loop of application starts
		If this function returns false, the application is terminated */
	appdata_s *ad = data;

	create_base_gui(ad);

	return true;
}

static void
app_control(app_control_h app_control, void *data)
{
	/* Handle the launch request. */
}

static void
app_pause(void *data)
{
	/* Take necessary actions when application becomes invisible. */
}

static void
app_resume(void *data)
{
	/* Take necessary actions when application becomes visible. */
}

static void
app_terminate(void *data)
{
	/* Release all resources. */
}

static void
ui_app_lang_changed(app_event_info_h event_info, void *user_data)
{
	/*APP_EVENT_LANGUAGE_CHANGED*/
	char *locale = NULL;
	system_settings_get_value_string(SYSTEM_SETTINGS_KEY_LOCALE_LANGUAGE, &locale);
	elm_language_set(locale);
	free(locale);
	return;
}

static void
ui_app_orient_changed(app_event_info_h event_info, void *user_data)
{
	/*APP_EVENT_DEVICE_ORIENTATION_CHANGED*/
	return;
}

static void
ui_app_region_changed(app_event_info_h event_info, void *user_data)
{
	/*APP_EVENT_REGION_FORMAT_CHANGED*/
}

static void
ui_app_low_battery(app_event_info_h event_info, void *user_data)
{
	/*APP_EVENT_LOW_BATTERY*/
}

static void
ui_app_low_memory(app_event_info_h event_info, void *user_data)
{
	/*APP_EVENT_LOW_MEMORY*/
}

//sensor event callback implementation
void sensor_event_callback(sensor_h sensor, sensor_event_s *event, void *user_data)
{
    sensor_type_e type;
    sensor_get_type(sensor, &type);
    if(type == SENSOR_ACCELEROMETER)
    {
    	acceValue.x=event->values[0];
    	acceValue.y=event->values[1];
    	acceValue.z=event->values[2];
    }else if(type==SENSOR_GYROSCOPE){
    	gyroValue.x=event->values[0];
    	gyroValue.y=event->values[1];
    	gyroValue.z=event->values[2];
    }
}

int
main(int argc, char *argv[])
{
	appdata_s ad = {0,};
	int ret = 0;

	ui_app_lifecycle_callback_s event_callback = {0,};
	app_event_handler_h handlers[5] = {NULL, };

	event_callback.create = app_create;
	event_callback.terminate = app_terminate;
	event_callback.pause = app_pause;
	event_callback.resume = app_resume;
	event_callback.app_control = app_control;

	sensor_h sensorAcce;
	sensor_listener_h listenerAcce;
	sensor_h sensorGyro;
	sensor_listener_h listenerGyro;
	device_power_request_lock(POWER_LOCK_DISPLAY, 0);

	//Starting sensor listener
	sensor_type_e acceType = SENSOR_ACCELEROMETER;
	sensor_type_e gyroType = SENSOR_GYROSCOPE;
	if (sensor_get_default_sensor(acceType, &sensorAcce) == SENSOR_ERROR_NONE&&sensor_get_default_sensor(gyroType, &sensorGyro) == SENSOR_ERROR_NONE){
		if ((sensor_create_listener(sensorAcce, &listenerAcce) == SENSOR_ERROR_NONE && sensor_listener_set_event_cb(listenerAcce,5, sensor_event_callback, NULL) == SENSOR_ERROR_NONE)
			&&(sensor_create_listener(sensorGyro, &listenerGyro) == SENSOR_ERROR_NONE && sensor_listener_set_event_cb(listenerGyro, 5, sensor_event_callback, NULL) == SENSOR_ERROR_NONE)){
			if (sensor_listener_set_option(listenerGyro, SENSOR_OPTION_ALWAYS_ON) == SENSOR_ERROR_NONE&&sensor_listener_set_option(listenerAcce, SENSOR_OPTION_ALWAYS_ON) == SENSOR_ERROR_NONE){
				if (sensor_listener_start(listenerAcce) == SENSOR_ERROR_NONE&&sensor_listener_start(listenerGyro) == SENSOR_ERROR_NONE){
					dlog_print(DLOG_INFO, LOG_TAG, "sensor listener started!");
				}
			}
		}
	}

	ui_app_add_event_handler(&handlers[APP_EVENT_LOW_BATTERY], APP_EVENT_LOW_BATTERY, ui_app_low_battery, &ad);
	ui_app_add_event_handler(&handlers[APP_EVENT_LOW_MEMORY], APP_EVENT_LOW_MEMORY, ui_app_low_memory, &ad);
	ui_app_add_event_handler(&handlers[APP_EVENT_DEVICE_ORIENTATION_CHANGED], APP_EVENT_DEVICE_ORIENTATION_CHANGED, ui_app_orient_changed, &ad);
	ui_app_add_event_handler(&handlers[APP_EVENT_LANGUAGE_CHANGED], APP_EVENT_LANGUAGE_CHANGED, ui_app_lang_changed, &ad);
	ui_app_add_event_handler(&handlers[APP_EVENT_REGION_FORMAT_CHANGED], APP_EVENT_REGION_FORMAT_CHANGED, ui_app_region_changed, &ad);

	createBluetoothConection();

	ret = ui_app_main(argc, argv, &event_callback, &ad);
	if (ret != APP_ERROR_NONE) {
		dlog_print(DLOG_ERROR, LOG_TAG, "app_main() is failed. err = %d", ret);
	}

	return ret;
}
