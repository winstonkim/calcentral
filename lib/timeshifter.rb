class Timeshifter

  include ClassLogger

  # substitute tokens in string with formatted date values from #Timeshifter.substitutions
  def self.process(str)
    Timeshifter.substitutions.each { |k, v| str.gsub!(k, v) }
    str
  end

  def self.substitutions
    @substitutions ||= build_substitutions
  end

  def self.build_substitutions

    # midnight on the current day
    today = Time.zone.today.in_time_zone.to_datetime
    end_of_week = today.sunday
    next_week = end_of_week.advance(days: 2)
    far_future = next_week.advance(days: 7)

    # Google tasks store due dates as zero-hour Z-time
    end_of_week_utc = today.sunday.to_date.to_datetime
    next_week_utc = end_of_week_utc.advance(days: 2)
    far_future_utc = next_week_utc.advance(days: 7)
    # Second Sunday in March
    utc_23_hour_day = today.end_of_year.advance(months: 2, days: 1).sunday.advance(weeks: 1)

    # Bearfacts XML often specifies dates as midnight with no timezone
    yesterday_no_tz = today.advance(:days => -1).to_date.to_datetime

    times = {
      ":::SEVEN_MONTHS_AGO:::" => today.advance(:months => -7).rfc3339,
      ":::FIVE_MONTHS_AGO:::" => today.advance(:months => -5).rfc3339,
      ":::TWENTY_SEVEN_DAYS_AGO:::" => today.advance(:days => -27).rfc3339,
      ":::TWO_WEEKS_AGO:::" => today.advance(:weeks => -2).rfc3339,
      ":::TWO_WEEKS_AGO_NO_TIME:::" => today.advance(:weeks => -2).strftime("%Y-%m-%d"),
      ":::DAY_BEFORE_YESTERDAY:::" => today.advance(:days => -2).rfc3339,
      ":::YESTERDAY:::" => today.advance(:days => -1).rfc3339,
      ":::TWO_DAYS_AGO_MIDNIGHT_PST:::" => today.advance(:days => -1, :minutes => -1).rfc3339,
      ":::TODAY_MIDNIGHT_PST:::" => today.rfc3339,
      ":::TOMORROW_MIDNIGHT_PST:::" => today.advance(:days => 1).rfc3339,
      ":::TODAY_AT_TEA_TIME:::" => today.advance(:hours => 15, :minutes => 47, :seconds => 13).rfc3339,
      ":::SIX_DAYS_HENCE:::" => today.advance(:days => 6).rfc3339,
      ":::SIX_DAYS_HENCE_NO_TIME:::" => today.advance(:days => 6).strftime("%Y-%m-%d"),
      ":::ONE_MONTH_HENCE:::" => today.advance(:months => 1).rfc3339,
      ":::FIVE_MONTHS_HENCE:::" => today.advance(:months => 5).rfc3339,
      ":::SEVEN_MONTHS_HENCE:::" => today.advance(:months => 7).rfc3339,
      ":::TODAY_EARLY:::" => today.advance(:hours => 0, :minutes => 05, :seconds => 00).rfc3339,
      ":::TODAY_LATE:::" => today.advance(:hours => 23, :minutes => 59, :seconds => 59).rfc3339,
      ":::TODAY_NINE:::" => today.advance(:hours => 9, :minutes => 00, :seconds => 00).rfc3339,
      ":::TODAY_TEN:::" => today.advance(:hours => 10, :minutes => 00, :seconds => 00).rfc3339,
      ":::TODAY_LUNCHTIME:::" => today.advance(:hours => 12, :minutes => 30, :seconds => 00).rfc3339,
      ":::TODAY_AFTER_LUNCH:::" => today.advance(:hours => 14, :minutes => 00, :seconds => 00).rfc3339,
      ":::TODAY_THREE_THIRTY:::" => today.advance(:hours => 15, :minutes => 30, :seconds => 00).rfc3339,
      ":::TODAY_FOUR_THIRTY:::" => today.advance(:hours => 16, :minutes => 30, :seconds => 00).rfc3339,
      ":::LATER_IN_WEEK:::" => end_of_week.rfc3339,
      ":::LATER_IN_WEEK_NO_TIME:::" => end_of_week.strftime("%Y-%m-%d"),
      ":::NEXT_WEEK:::" => next_week.rfc3339,
      ":::FAR_FUTURE:::" => far_future.rfc3339,
      ":::UTC_LATER_IN_WEEK:::" => end_of_week_utc.strftime('%FT%T.000Z'),
      ":::UTC_NEXT_WEEK:::" => next_week_utc.strftime('%FT%T.000Z'),
      ":::UTC_FAR_FUTURE:::" => far_future_utc.strftime('%FT%T.000Z'),
      ":::UTC_23_HOUR_DAY:::" => utc_23_hour_day.strftime('%FT%T.000Z'),
      ":::TODAY:::" => today.rfc3339,
      ":::TODAY_NO_TIME:::" => today.strftime("%Y-%m-%d"),
      ":::TOMORROW:::" => today.advance(days: 1).rfc3339,
      ":::TOMORROW_NO_TIME:::" => today.advance(days: 1).strftime("%Y-%m-%d"),
      ":::DAY_AFTER_TOMORROW:::" => today.advance(days: 2).rfc3339,
      ":::NO_TZ_DAY_BEFORE_YESTERDAY:::" => yesterday_no_tz.advance(:days => -1).strftime('%F %T.0'),
      ":::NO_TZ_YESTERDAY:::" => yesterday_no_tz.strftime('%F %T.0')
    }

    logger.info "Today = #{today}; epoch = #{today.to_time.to_i}"
    logger.info "Substitutions = #{times.inspect}"

    times
  end

end
