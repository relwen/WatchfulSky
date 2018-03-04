package ca.mun.engi5895.stargazer;

/**
 * The Entity class is a representation of a space entity, with many methods used to interpret
 its data. It is important to note here that the data on each satellite is in TLE (Two Line
 Element) format. Many of the Entity class methods call various methods from the TLE,
 TLEPropagator, PVCoordinates and TimeScaleFactory classes. As previously mentioned,
 the Orekit open source library is used for the data calculations. The TLE, TLEPropagator,
 and TimescaleFactory classes are both part of the Orekit library, as well as Vector3D,
 BodyCenterPointing, AttitudeProvider, AbsoluteDate, TimeScale, PVCoordinates, Ellipsoid,
 and Frame. Lots of these classes must communicate with one another for the data calculations
 to execute correctly and for entity to be able to access the required information.
 */

import org.orekit.attitudes.Attitude;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.TLEPropagatorBuilder;
import org.orekit.propagation.Propagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.frames.Frame;
import org.orekit.bodies.Ellipsoid;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.AngularCoordinates;

import org.hipparchus.geometry.euclidean.threed.Vector3D; //may need hipparchus core
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Entity {

    private static TLE entity; //TLE object for satellite
    private static Propagator tleProp;
    /*
    private Calendar calendar;
    private double[] array;
    private int avgMass = 200;
    private Attitude att;
    private PVCoordinates currentPV;
    private TLEPropagatorBuilder builder;
    */

    //constructor
    Entity(String line1, String line2) throws OrekitException{

        entity = new TLE(line1, line2); //creates TLE object
        tleProp = TLEPropagator.selectExtrapolator(entity); //extrapolates proper propagation for orbit as TLEPropagator

       /*
       TimeScale timeZone = TimeScalesFactory.getUTC();
       Date date = new Date();
       calendar = GregorianCalendar.getInstance();
       calendar.setTime(date);
       Frame frame = FramesFactory.getGCRF();
       builder = new TLEPropagatorBuilder(entity, PositionAngle.MEAN,1);
       Ellipsoid el = new Ellipsoid(frame, 1,1,1);
       AngularCoordinates orientation = new AngularCoordinates();
       AbsoluteDate initialDate = new AbsoluteDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), timeZone);
       currentPV = new PVCoordinates();
       att = new Attitude(initialDate, frame, orientation);
       Attitude att = new Attitude( initialDate, frame, orientation);
       */
    }

    //In design documentation this is referred to as "update()"
    //returns updated coordinates of satellite
    private TimeStampedPVCoordinates getPVCoordinates() throws OrekitException {
        TimeScale timeZone = TimeScalesFactory.getUTC(); // get UTC time scale
        Date date = new Date(); //creates date
        Calendar calendar = GregorianCalendar.getInstance(); //sets calendar
        calendar.setTime(date); //updates date and time
        AbsoluteDate abDate = new AbsoluteDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), timeZone); //creates orekit absolute date from calender
        Frame frame = FramesFactory.getGCRF();
        return tleProp.getPVCoordinates(abDate, frame); //returns coordinates

    }
    //Returns the magnitude of the velocity
    public double getVelocity() throws OrekitException {
        PVCoordinates coord =this.getPVCoordinates(); //gets up to date coordinates
        Vector3D velocity = coord.getVelocity(); //gets velocity vector
        //get components of vector as doubles
        double x = velocity.getX();
        double y = velocity.getY();
        double z = velocity.getZ();
        return Math.sqrt(x*x + y*y + z*z); //returns magnitude
    }
    //returns orbital period of satellite
    public double getPeriod(){

        double meanMotion = entity.getMeanMotion(); //gets mean motion
        return 1 / meanMotion; //returns period
    }

    //gets Height of satellite from ground
    public double getHeight() throws OrekitException{
        PVCoordinates coord =this.getPVCoordinates(); //gets up to date coordinates
        Vector3D position = coord.getPosition();
        return position.getZ(); // MAY BE WRONG VARIABLE !! MAY NEED TO USE getX() or getY()
    }

    public double getPerigee(){

        return 0;
    }

    public double getApogee() {

        return 0;
    }


    public double getInclination(){

        return 0;
    }
}