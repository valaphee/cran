/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.flow.radio.sdr

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.ByteBuffer

/**
 * @author Kevin Ludwig
 */
interface LibRtlSdr : Library {
    fun rtlsdr_get_device_count(): Int

    fun rtlsdr_get_device_name(index: Int): String

    /*!
     * Get USB device strings.
     *
     * NOTE: The string arguments must provide space for up to 256 bytes.
     *
     * \param index the device index
     * \param manufact manufacturer name, may be NULL
     * \param product product name, may be NULL
     * \param serial serial number, may be NULL
     * \return 0 on success
     */
    fun rtlsdr_get_device_usb_strings(index: Int, manufact: String?, product: String?, serial: String?): Int

    /*!
     * Get device index by USB serial string descriptor.
     *
     * \param serial serial string of the device
     * \return device index of first device where the name matched
     * \return -1 if name is NULL
     * \return -2 if no devices were found at all
     * \return -3 if devices were found, but none with matching name
     */
    fun rtlsdr_get_index_by_serial(serial: String): Int

    fun rtlsdr_open(dev: PointerByReference, index: Int): Int

    fun rtlsdr_close(dev: Pointer): Int

    /* configuration functions */

    /*!
     * Set crystal oscillator frequencies used for the RTL2832 and the tuner IC.
     *
     * Usually both ICs use the same clock. Changing the clock may make sense if
     * you are applying an external clock to the tuner or to compensate the
     * frequency (and samplerate) error caused by the original (cheap) crystal.
     *
     * NOTE: Call this function only if you fully understand the implications.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param rtl_freq frequency value used to clock the RTL2832 in Hz
     * \param tuner_freq frequency value used to clock the tuner IC in Hz
     * \return 0 on success
     */
    fun rtlsdr_set_xtal_freq(dev: Pointer, rtl_freq: Int, tuner_freq: Int): Int

    /*!
     * Get crystal oscillator frequencies used for the RTL2832 and the tuner IC.
     *
     * Usually both ICs use the same clock.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param rtl_freq frequency value used to clock the RTL2832 in Hz
     * \param tuner_freq frequency value used to clock the tuner IC in Hz
     * \return 0 on success
     */
    fun rtlsdr_get_xtal_freq(dev: Pointer, rtl_freq: IntByReference, tuner_freq: IntByReference): Int

    /*!
     * Get USB device strings.
     *
     * NOTE: The string arguments must provide space for up to 256 bytes.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param manufact manufacturer name, may be NULL
     * \param product product name, may be NULL
     * \param serial serial number, may be NULL
     * \return 0 on success
     */
    fun rtlsdr_get_usb_strings(dev: Pointer, manufact: String?, product: String?, serial: String?): Int

    /*!
     * Write the device EEPROM
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param data buffer of data to be written
     * \param offset address where the data should be written
     * \param len length of the data
     * \return 0 on success
     * \return -1 if device handle is invalid
     * \return -2 if EEPROM size is exceeded
     * \return -3 if no EEPROM was found
     */
    fun rtlsdr_write_eeprom(dev: Pointer, data: ByteArray, offset: Byte, len: Short): Int

    /*!
     * Read the device EEPROM
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param data buffer where the data should be written
     * \param offset address where the data should be read from
     * \param len length of the data
     * \return 0 on success
     * \return -1 if device handle is invalid
     * \return -2 if EEPROM size is exceeded
     * \return -3 if no EEPROM was found
     */
    fun rtlsdr_read_eeprom(dev: Pointer, data: ByteArray, offset: Byte, len: Short): Int

    fun rtlsdr_set_center_freq(dev: Pointer, freq: Int): Int

    /*!
     * Get actual frequency the device is tuned to.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return 0 on error, frequency in Hz otherwise
     */
    fun rtlsdr_get_center_freq(dev: Pointer): Int

    /*!
     * Set the frequency correction value for the device.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param ppm correction value in parts per million (ppm)
     * \return 0 on success
     */
    fun rtlsdr_set_freq_correction(dev: Pointer, ppm: Int): Int

    /*!
     * Get actual frequency correction value of the device.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return correction value in parts per million (ppm)
     */
    fun rtlsdr_get_freq_correction(dev: Pointer): Int

    interface rtlsdr_tuner {
        companion object {
            const val RTLSDR_TUNER_UNKNOWN = 0
            const val RTLSDR_TUNER_E4000 = 1
            const val RTLSDR_TUNER_FC0012 = 2
            const val RTLSDR_TUNER_FC0013 = 3
            const val RTLSDR_TUNER_FC2580 = 4
            const val RTLSDR_TUNER_R820T = 5
            const val RTLSDR_TUNER_R828D = 6
        }
    }

    /*!
     * Get the tuner type.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return RTLSDR_TUNER_UNKNOWN on error, tuner type otherwise
     */
    fun rtlsdr_get_tuner_type(dev: Pointer): Int

    /*!
     * Get a list of gains supported by the tuner.
     *
     * NOTE: The gains argument must be preallocated by the caller. If NULL is
     * being given instead, the number of available gain values will be returned.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param gains array of gain values. In tenths of a dB, 115 means 11.5 dB.
     * \return <= 0 on error, number of available (returned) gain values otherwise
     */
    fun rtlsdr_get_tuner_gains(dev: Pointer, gains: IntArray?): Int

    /*!
     * Set the gain for the device.
     * Manual gain mode must be enabled for this to work.
     *
     * Valid gain values (in tenths of a dB) for the E4000 tuner:
     * -10, 15, 40, 65, 90, 115, 140, 165, 190,
     * 215, 240, 290, 340, 420, 430, 450, 470, 490
     *
     * Valid gain values may be queried with \ref rtlsdr_get_tuner_gains function.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param gain in tenths of a dB, 115 means 11.5 dB.
     * \return 0 on success
     */
    fun rtlsdr_set_tuner_gain(dev: Pointer, gain: Int): Int

    /*!
     * Set the bandwidth for the device.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param bw bandwidth in Hz. Zero means automatic BW selection.
     * \return 0 on success
     */
    fun rtlsdr_set_tuner_bandwidth(dev: Pointer, bw: Int): Int

    /*!
     * Get actual gain the device is configured to.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return 0 on error, gain in tenths of a dB, 115 means 11.5 dB.
     */
    fun rtlsdr_get_tuner_gain(dev: Pointer): Int

    /*!
     * Set the intermediate frequency gain for the device.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param stage intermediate frequency gain stage number (1 to 6 for E4000)
     * \param gain in tenths of a dB, -30 means -3.0 dB.
     * \return 0 on success
     */
    fun rtlsdr_set_tuner_if_gain(dev: Pointer, stage: Int, gain: Int): Int

    /*!
     * Set the gain mode (automatic/manual) for the device.
     * Manual gain mode must be enabled for the gain setter function to work.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param manual gain mode, 1 means manual gain mode shall be enabled.
     * \return 0 on success
     */
    fun rtlsdr_set_tuner_gain_mode(dev: Pointer, manual: Int): Int

    /*!
     * Set the sample rate for the device, also selects the baseband filters
     * according to the requested sample rate for tuners where this is possible.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param samp_rate the sample rate to be set, possible values are:
     * 		    225001 - 300000 Hz
     * 		    900001 - 3200000 Hz
     * 		    sample loss is to be expected for rates > 2400000
     * \return 0 on success, -EINVAL on invalid rate
     */
    fun rtlsdr_set_sample_rate(dev: Pointer, rate: Int): Int

    /*!
     * Get actual sample rate the device is configured to.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return 0 on error, sample rate in Hz otherwise
     */
    fun rtlsdr_get_sample_rate(dev: Pointer): Int

    /*!
     * Enable test mode that returns an 8 bit counter instead of the samples.
     * The counter is generated inside the RTL2832.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param test mode, 1 means enabled, 0 disabled
     * \return 0 on success
     */
    fun rtlsdr_set_testmode(dev: Pointer, on: Int): Int

    /*!
     * Enable or disable the internal digital AGC of the RTL2832.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param digital AGC mode, 1 means enabled, 0 disabled
     * \return 0 on success
     */
    fun rtlsdr_set_agc_mode(dev: Pointer, on: Int): Int

    /*!
     * Enable or disable the direct sampling mode. When enabled, the IF mode
     * of the RTL2832 is activated, and rtlsdr_set_center_freq() will control
     * the IF-frequency of the DDC, which can be used to tune from 0 to 28.8 MHz
     * (xtal frequency of the RTL2832).
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param on 0 means disabled, 1 I-ADC input enabled, 2 Q-ADC input enabled
     * \return 0 on success
     */
    fun rtlsdr_set_direct_sampling(dev: Pointer, on: Int): Int

    /*!
     * Get state of the direct sampling mode
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return -1 on error, 0 means disabled, 1 I-ADC input enabled
     *	    2 Q-ADC input enabled
     */
    fun rtlsdr_get_direct_sampling(dev: Pointer): Int

    /*!
     * Enable or disable offset tuning for zero-IF tuners, which allows to avoid
     * problems caused by the DC offset of the ADCs and 1/f noise.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param on 0 means disabled, 1 enabled
     * \return 0 on success
     */
    fun rtlsdr_set_offset_tuning(dev: Pointer, on: Int): Int

    /*!
     * Get state of the offset tuning mode
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return -1 on error, 0 means disabled, 1 enabled
     */
    fun rtlsdr_get_offset_tuning(dev: Pointer): Int

    /* streaming functions */

    fun rtlsdr_reset_buffer(dev: Pointer): Int

    fun rtlsdr_read_sync(dev: Pointer, buf: ByteBuffer, len: Int, n_read: IntByReference): Int

    interface rtlsdr_read_async_cb_t : Callback {
        fun apply(buf: Pointer, len: Int, ctx: Pointer?)
    }

    /*!
     * Read samples from the device asynchronously. This function will block until
     * it is being canceled using rtlsdr_cancel_async()
     *
     * NOTE: This function is deprecated and is subject for removal.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param cb callback function to return received samples
     * \param ctx user specific context to pass via the callback function
     * \return 0 on success
     */
    fun rtlsdr_wait_async(dev: Pointer, cb: rtlsdr_read_async_cb_t, ctx: Pointer?): Int

    /*!
     * Read samples from the device asynchronously. This function will block until
     * it is being canceled using rtlsdr_cancel_async()
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param cb callback function to return received samples
     * \param ctx user specific context to pass via the callback function
     * \param buf_num optional buffer count, buf_num * buf_len = overall buffer size
     *		  set to 0 for default buffer count (15)
     * \param buf_len optional buffer length, must be multiple of 512,
     *		  should be a multiple of 16384 (URB size), set to 0
     *		  for default buffer length (16 * 32 * 512)
     * \return 0 on success
     */
    fun rtlsdr_read_async(dev: Pointer, cb: rtlsdr_read_async_cb_t, ctx: Pointer?, buf_num: Int, buf_len: Int): Int

    /*!
     * Cancel all pending asynchronous operations on the device.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \return 0 on success
     */
    fun rtlsdr_cancel_async(dev: Pointer): Int

    /*!
     * Enable or disable the bias tee on GPIO PIN 0.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param on  1 for Bias T on. 0 for Bias T off.
     * \return -1 if device is not initialized. 0 otherwise.
     */
    fun rtlsdr_set_bias_tee(dev: Pointer, on: Int): Int

    /*!
     * Enable or disable the bias tee on the given GPIO pin.
     *
     * \param dev the device handle given by rtlsdr_open()
     * \param gpio the gpio pin to configure as a Bias T control.
     * \param on  1 for Bias T on. 0 for Bias T off.
     * \return -1 if device is not initialized. 0 otherwise.
     */
    fun rtlsdr_set_bias_tee_gpio(dev: Pointer, gpio: Int, on: Int): Int

    companion object {
        val Instance: LibRtlSdr = Native.load("librtlsdr", LibRtlSdr::class.java)
    }
}
