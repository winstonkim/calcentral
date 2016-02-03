module CampusSolutions
  module EnrollmentTermExpiry
    def self.expire(uid=nil)
      [
        MyEnrollmentTerm,
        MyEnrollmentTerms
      ].each do |klass|
        klass.expire uid
      end
    end
  end
end
