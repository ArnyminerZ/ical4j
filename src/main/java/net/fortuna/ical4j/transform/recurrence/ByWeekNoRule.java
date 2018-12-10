package net.fortuna.ical4j.transform.recurrence;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.util.Dates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;

/**
 * Applies BYWEEKNO rules specified in this Recur instance to the specified date list. If no BYWEEKNO rules are
 * specified the date list is returned unmodified.
 */
public class ByWeekNoRule extends AbstractRecurrenceRule {

    private transient Logger log = LoggerFactory.getLogger(ByWeekNoRule.class);

    private final NumberList weekNoList;

    public ByWeekNoRule(NumberList weekNoList) {
        this.weekNoList = weekNoList;
    }

    public ByWeekNoRule(NumberList weekNoList, Optional<WeekDay.Day> weekStartDay) {
        super(weekStartDay);
        this.weekNoList = weekNoList;
    }

    @Override
    public DateList transform(DateList dates) {
        if (weekNoList.isEmpty()) {
            return dates;
        }
        // XXX: Need to find a way to provide day of week to apply to candidates
//        final int initDayOfWeek = rootSeed.get(Calendar.DAY_OF_WEEK);
        final DateList weekNoDates = Dates.getDateListInstance(dates);
        Calendar initCal = getCalendarInstance(dates.get(0), true);
//        int initDayOfWeek = initCal.get(Calendar.DAY_OF_WEEK);
        for (final Date date : dates) {
            final int numWeeksInYear = initCal.getActualMaximum(Calendar.WEEK_OF_YEAR);
            for (final Integer weekNo : weekNoList) {
                if (weekNo == 0 || weekNo < -Dates.MAX_WEEKS_PER_YEAR || weekNo > Dates.MAX_WEEKS_PER_YEAR) {
                    if (log.isTraceEnabled()) {
                        log.trace("Invalid week of year: " + weekNo);
                    }
                    continue;
                }
                final Calendar cal = getCalendarInstance(date, true);
                if (weekNo > 0) {
                    if (numWeeksInYear < weekNo) {
                        continue;
                    }
                    cal.set(Calendar.WEEK_OF_YEAR, weekNo);
                } else {
                    if (numWeeksInYear < -weekNo) {
                        continue;
                    }
                    cal.set(Calendar.WEEK_OF_YEAR, numWeeksInYear);
                    cal.add(Calendar.WEEK_OF_YEAR, weekNo + 1);
                }
//                cal.set(Calendar.DAY_OF_WEEK, initDayOfWeek);
                weekNoDates.add(Dates.getInstance(cal.getTime(), weekNoDates.getType()));
            }
        }
        return weekNoDates;
    }

    /**
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        log = LoggerFactory.getLogger(Recur.class);
    }
}
