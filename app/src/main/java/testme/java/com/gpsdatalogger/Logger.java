package testme.java.com.gpsdatalogger;

import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.DecimalFormat;

import testme.java.com.gpsdatalogger.interfaces.GnssListener;

/**
 * Created by achau on 30-01-2018.
 */

public class Logger implements GnssListener {

    public synchronized GpsLoggerActivity.UIComponent getComponent() {
        return component;
    }

    public synchronized void setComponent(GpsLoggerActivity.UIComponent component) {
        this.component = component;
    }

    private GpsLoggerActivity.UIComponent component;

    @Override
    public void onProviderEnabled(String provider) {
        logLocationEvent(Constants.PROVIDER_ENABLED + provider+"\n");
    }

    @Override
    public void onProviderDisabled(String provider) {
        logLocationEvent(Constants.PROVIDER_DISABLED  + provider+"\n");
    }

    @Override
    public void onLocationChanged(Location location) {
        logLocationEvent(Constants.LOCATION_CHANGED +"\n"+ "LATITUDE" +location.getLatitude() + "\n" + "LATITUDE" + location.getLatitude());
    }

    @Override
    public void onLocationStatusChanged(String provider, int status, Bundle extras) {
        String message =
                String.format(
                        Constants.LOCATION_STATUS_CHANGED ,
                    //    "onStatusChanged: provider=%s, status=%s, extras=%s",
                        provider, locationStatusToString(status), extras +"\n");
        logLocationEvent(message);
    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        StringBuilder builder = new StringBuilder(Constants.GNSS_MEASUREMENT_EVENT + "\n");

        builder.append(Constants.GNSS_CLOCK_INFORMATION +toStringClock(event.getClock()) + "\n");
        builder.append(Constants.GNSS_INFO + "\n") ;

        for (GnssMeasurement measurement : event.getMeasurements()) {
            builder.append(Constants.GNSS_DELTA_ACCUMULATED_LAST_CHANNEL + measurement.getAccumulatedDeltaRangeMeters() + "/n");
            builder.append(Constants.GNSS_ACCUMULATED_CHANNEL + measurement.getAccumulatedDeltaRangeState() + "/n");
            builder.append(Constants.GNSS_DELTA_ACCUMULATED_UNCERTAINITY + measurement.getAccumulatedDeltaRangeUncertaintyMeters() + "/n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.append(Constants.GNSS_AUTO_CONTROL_LEVEL_DB + measurement.getAutomaticGainControlLevelDb() + "/n");
            }
            builder.append( Constants.GNSS_NUMBER_OF_CARRIER_CYCLES + measurement.getCarrierCycles() + "/n");
            builder.append(Constants.GNSS_CARRIER_PHASE + measurement.getCarrierPhase() + "/n");
            builder.append(Constants.GNSS_CARRIER_PHASE_UNCERTAINITY + measurement.getCarrierPhaseUncertainty() + "/n");
            builder.append(Constants.GNSS_CARRIER_TO_NOISE_DENSITY + measurement.getCn0DbHz() + "/n");
            builder.append(Constants.GNSS_PSEUDO_RANGE_RATE + measurement.getPseudorangeRateMetersPerSecond() + "/n");
            builder.append(Constants.GNSS_ESTIMATED_TIME_ERROR + measurement.getReceivedSvTimeUncertaintyNanos() + "/n");
        }
    }

    @Override
    public void onGnssMeasurementsStatusChanged(int status) {
        logMeasurementEvent(Constants.GNSS_MEASUREMENT_STATUS_CHANGED + gnssMeasurementsStatusToString(status) + "\n");
    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
        logNavigationMessageEvent(Constants.GNSS_NAVAIGATION_MESSAGE + event + "\n");
    }

    @Override
    public void onGnssNavigationMessageStatusChanged(int status) {
        logNavigationMessageEvent(Constants.GNSS_NAVAIGATION_MESSAGE_CHANGED  + getGnssNavigationMessageStatus(status) + "\n");
    }

    @Override
    public void onGnssStatusChanged(GnssStatus gnssStatus) {
        logStatusEvent(Constants.GNSS_STATUS_CHANGED + gnssStatusToString(gnssStatus) + "\n");
    }

    @Override
    public void onListenerRegistration(String listener, boolean result) {
        logEvent(Constants.REGISTRATION, String.format("add%sListener: %b", listener, result) + "\n");
    }

    @Override
    public void onNmeaReceived(long l, String s) {
        logNmeaEvent(String.format( "ON_NMEA_RECEIVED :" +"timestamp=%d, %s", l, s) + "\n");
    }

    @Override
    public void onTTFFReceived(long l) {
        logNmeaEvent(String.format( "ON TTFF_RECEIVED :" +""+l) + "\n");
    }

    private void logLocationEvent(String event) {
        logEvent("LOCATION_EVENT", event + "\n");
    }

    private void logEvent(String tag, String message) {
        String composedTag = Constants.GNSS_LOGGER + tag;
        Log.d(composedTag, message);

        GpsLoggerActivity.UIComponent component = getComponent();
        if (component != null) {
            component.logTextFragment(tag, message);
        }
    }

    private String locationStatusToString(int status) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                return "AVAILABLE";
            case LocationProvider.OUT_OF_SERVICE:
                return "OUT_OF_SERVICE";
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                return "TEMPORARILY_UNAVAILABLE";
            default:
                return "<UNKNOWN>";
        }
    }

    private String toStringClock(GnssClock gnssClock) {
        final String format = "   %-4s = %s\n";
        StringBuilder builder = new StringBuilder("GNSS_CLOCK :\n");
        DecimalFormat numberFormat = new DecimalFormat("#0.000");
        if (gnssClock.hasLeapSecond()) {
            builder.append(String.format(format, "CLOCK_LEAP_SECOND : ", gnssClock.getLeapSecond()) + "\n");
        }

        builder.append(String.format(format, "CLOCK_TIME_NANOS : ", gnssClock.getTimeNanos()) + "\n");
        if (gnssClock.hasTimeUncertaintyNanos()) {
            builder.append(
                    String.format(format, "CLOCK_UNCERTAINITY_NANOS : ", gnssClock.getTimeUncertaintyNanos())+ "\n");
        }

        if (gnssClock.hasFullBiasNanos()) {
            builder.append(String.format(format, "CLOCK_FULL_BIAS_NANOS : ", gnssClock.getFullBiasNanos()) + "\n");
        }

        if (gnssClock.hasBiasNanos()) {
            builder.append(String.format(format, "CLOCK_BIAS_NANOS : ", gnssClock.getBiasNanos()) + "\n");
        }
        if (gnssClock.hasBiasUncertaintyNanos()) {
            builder.append(
                    String.format(
                            format,
                            "BIAS_UNCERTAINITY_NANOS :",
                            numberFormat.format(gnssClock.getBiasUncertaintyNanos())) + "\n");
        }

        if (gnssClock.hasDriftNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DRIFT_NANOS_PER_SECOND : ",
                            numberFormat.format(gnssClock.getDriftNanosPerSecond())) + "\n");
        }

        if (gnssClock.hasDriftUncertaintyNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DRIFT_UNCERTAINITY_NANOS : ",
                            numberFormat.format(gnssClock.getDriftUncertaintyNanosPerSecond())) + "\n");
        }

        builder.append(
                String.format(
                        format,
                        "HARDWARE_CLOCK_DISCONTINUITY_COUNT : ",
                        gnssClock.getHardwareClockDiscontinuityCount()) + "\n");

        return builder.toString();
    }

    private String toStringMeasurement(GnssMeasurement measurement) {
        final String format = "   %-4s = %s\n";
        StringBuilder builder = new StringBuilder("GnssMeasurement:\n");
        DecimalFormat numberFormat = new DecimalFormat("#0.000");
        DecimalFormat numberFormat1 = new DecimalFormat("#0.000E00");
        builder.append(String.format(format, "Svid", measurement.getSvid()));
        builder.append(String.format(format, "ConstellationType", measurement.getConstellationType()));
        builder.append(String.format(format, "TimeOffsetNanos", measurement.getTimeOffsetNanos()));

        builder.append(String.format(format, "State", measurement.getState()));

        builder.append(
                String.format(format, "ReceivedSvTimeNanos", measurement.getReceivedSvTimeNanos()));
        builder.append(
                String.format(
                        format,
                        "ReceivedSvTimeUncertaintyNanos",
                        measurement.getReceivedSvTimeUncertaintyNanos()));

        builder.append(String.format(format, "Cn0DbHz", numberFormat.format(measurement.getCn0DbHz())));

        builder.append(
                String.format(
                        format,
                        "PseudorangeRateMetersPerSecond",
                        numberFormat.format(measurement.getPseudorangeRateMetersPerSecond())));
        builder.append(
                String.format(
                        format,
                        "PseudorangeRateUncertaintyMetersPerSeconds",
                        numberFormat.format(measurement.getPseudorangeRateUncertaintyMetersPerSecond())));

        if (measurement.getAccumulatedDeltaRangeState() != 0) {
            builder.append(
                    String.format(
                            format, "AccumulatedDeltaRangeState", measurement.getAccumulatedDeltaRangeState()));

            builder.append(
                    String.format(
                            format,
                            "AccumulatedDeltaRangeMeters",
                            numberFormat.format(measurement.getAccumulatedDeltaRangeMeters())));
            builder.append(
                    String.format(
                            format,
                            "AccumulatedDeltaRangeUncertaintyMeters",
                            numberFormat1.format(measurement.getAccumulatedDeltaRangeUncertaintyMeters())));
        }

        if (measurement.hasCarrierFrequencyHz()) {
            builder.append(
                    String.format(format, "CarrierFrequencyHz", measurement.getCarrierFrequencyHz()));
        }

        if (measurement.hasCarrierCycles()) {
            builder.append(String.format(format, "CarrierCycles", measurement.getCarrierCycles()));
        }

        if (measurement.hasCarrierPhase()) {
            builder.append(String.format(format, "CarrierPhase", measurement.getCarrierPhase()));
        }

        if (measurement.hasCarrierPhaseUncertainty()) {
            builder.append(
                    String.format(
                            format, "CarrierPhaseUncertainty", measurement.getCarrierPhaseUncertainty()));
        }

        builder.append(
                String.format(format, "MultipathIndicator", measurement.getMultipathIndicator()));

        if (measurement.hasSnrInDb()) {
            builder.append(String.format(format, "SnrInDb", measurement.getSnrInDb()));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (measurement.hasAutomaticGainControlLevelDb()) {
                builder.append(
                        String.format(format, "AgcDb", measurement.getAutomaticGainControlLevelDb()));
            }
            if (measurement.hasCarrierFrequencyHz()) {
                builder.append(String.format(format, "CarrierFreqHz", measurement.getCarrierFrequencyHz()));
            }
        }

        return builder.toString();
    }

    private void logMeasurementEvent(String event) {
        logEvent("MEASUREMENT : ", event +  "\n");
    }

    private String gnssMeasurementsStatusToString(int status) {
        switch (status) {
            case GnssMeasurementsEvent.Callback.STATUS_NOT_SUPPORTED:
                logEvent("MEASUREMENT : ", "---------GNSS_NOT_SUPPORTED-------" +  "\n");
                return "GNSS_NOT_SUPPORTED";
            case GnssMeasurementsEvent.Callback.STATUS_READY:
                return "READY";
            case GnssMeasurementsEvent.Callback.STATUS_LOCATION_DISABLED:
                logEvent("MEASUREMENT : ", "---------GNSS_LOCATION_DISABLED-------" +  "\n");
                return "GNSS_LOCATION_DISABLED";
            default:
                return "<UKNOWN>";
        }
    }

    private String getGnssNavigationMessageStatus(int status) {
        switch (status) {
            case GnssNavigationMessage.STATUS_UNKNOWN:
                return "STATUS_UNKNOWN";
            case GnssNavigationMessage.STATUS_PARITY_PASSED:
                return "READY";
            case GnssNavigationMessage.STATUS_PARITY_REBUILT:
                return "STATUS_PARITY_REBUILT";
            default:
                return "<UNKNOWN>";
        }
    }

    private void logNavigationMessageEvent(String event) {
        logEvent("NAVIGATION_NESSAGE_EVENT", event + "\n");
    }

    private void logStatusEvent(String event) {
        logEvent("STATUS_EVENT", event  + "\n");
    }

    private String gnssStatusToString(GnssStatus gnssStatus) {

        StringBuilder builder = new StringBuilder("SATELLITE_STATUS | [SATELLITES:\n");
        for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
            builder
                    .append("GNSS_STATUS_CONSTELLATION_NAME = " + "\n")
                    .append(getConstellationName(gnssStatus.getConstellationType(i)))
                    .append("\n");
            builder.append("GNSS_STATUS_SVID :  ").append(gnssStatus.getSvid(i)).append("\n");
            builder.append("GNSS_STAUS_NOISE_DENSITY = ").append(gnssStatus.getCn0DbHz(i)).append("\n");
            builder.append("GNSS_STATUS_ELEVATION").append(gnssStatus.getElevationDegrees(i)).append("\n");
            builder.append("GNSS_STATUS_AZIMUTH").append(gnssStatus.getAzimuthDegrees(i)).append("\n");
            builder.append("GNSS_STAUTS_EPEMERIC = ").append(gnssStatus.hasEphemerisData(i)).append("\n");
            builder.append("GNSS_STATUS_ALMANC_DATA = ").append(gnssStatus.hasAlmanacData(i)).append("\n");
            builder.append("GNSS_STATUS_USED_IN_FIX= ").append(gnssStatus.usedInFix(i)).append("\n");
        }
        builder.append("]");
        return builder.toString();
    }

    private String getConstellationName(int id) {
        switch (id) {
            case 1:
                return "GPS";
            case 2:
                return "SBAS";
            case 3:
                return "GLONASS";
            case 4:
                return "QZSS";
            case 5:
                return "BEIDOU";
            case 6:
                return "GALILEO";
            default:
                return "UNKNOWN";
        }
    }
    private void logNmeaEvent(String event) {
        logEvent("NMEA_EVENT", event);
    }



}