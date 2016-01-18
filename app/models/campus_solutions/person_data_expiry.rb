module CampusSolutions
  module PersonDataExpiry
    def self.expire(uid=nil)
      HubEdos::MyStudent.expire uid
    end
  end
end
