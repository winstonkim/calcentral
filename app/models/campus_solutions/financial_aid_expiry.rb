module CampusSolutions
  module FinancialAidExpiry
    def self.expire(uid=nil)
      [
        MyAidYears,
        MyFinancialAidData,
        MyFinancialAidFundingSources,
        MyFinancialAidFundingSourcesTerm
      ].each do |klass|
        klass.expire uid
      end
    end
  end
end
