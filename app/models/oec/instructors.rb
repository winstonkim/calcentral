module Oec
  class Instructors < Worksheet

    def headers
      %w(
        LDAP_UID
        SIS_ID
        FIRST_NAME
        LAST_NAME
        EMAIL_ADDRESS
      )
    end

    validate('LDAP_UID') { |row| 'Non-numeric' unless row['LDAP_UID'] =~ /\A\d+\Z/ }
    validate('SIS_ID') { |row| 'Unexpected' unless row['SIS_ID'] == "UID:#{row['LDAP_UID']}" || row['SIS_ID'] =~ /\A\d+\Z/ }

  end
end
