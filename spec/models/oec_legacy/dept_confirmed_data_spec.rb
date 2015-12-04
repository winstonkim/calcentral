describe OecLegacy::DeptConfirmedData do

  it 'should load all courses_confirmed.csv files when no departments specified' do
    actual_csv_files = %w(INTEGBI POL\ SCI STAT)
    missing_csv_files = %w(FOO BAZ)
    confirmed_data = OecLegacy::DeptConfirmedData.new('fixtures/oec_legacy', actual_csv_files + missing_csv_files)
    confirmed_data_hash = confirmed_data.confirmed_data_per_dept
    confirmed_data_hash.keys.should match_array actual_csv_files
    warnings = confirmed_data.warnings_per_dept
    warnings.length.should eq 4
    warnings['POL SCI']['WARN'].length.should eq 4
    warnings['STAT']['WARN'].length.should eq 8
  end

  it 'should not load the STAT_courses_confirmed.csv because it was not requested' do
    csv_files = %w(INTEGBI POL\ SCI)
    dept_set = OecLegacy::DepartmentRegistry.new csv_files
    confirmed_data = OecLegacy::DeptConfirmedData.new('fixtures/oec_legacy', dept_set)
    confirmed_data.confirmed_data_per_dept.keys.should match_array csv_files
    # Expect report of missing MCELLBI and INTEGBI files because BIOLOGY was requested
    confirmed_data.warnings_per_dept.length.should eq 3
    warnings = confirmed_data.warnings_per_dept['POL SCI']['WARN']
    warnings[2] =~ /2015-B-71424-1002514(.*)duplicate/
    warnings[3] =~ /2015-B-71419 (.*)duplicate/
  end

  it 'should disallow BIOLOGY' do
    dept_set = OecLegacy::DepartmentRegistry.new %w(BIOLOGY)
    confirmed_data = OecLegacy::DeptConfirmedData.new('fixtures/oec_legacy', dept_set)
    warnings = confirmed_data.warnings_per_dept['BIOLOGY']['WARN']
    warnings.length.should eq 1
    warnings[0] =~ /BIOLOGY(.*)is not allowed(.*)/
  end

end
