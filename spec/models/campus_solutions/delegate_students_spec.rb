describe CampusSolutions::DelegateStudents do
  let(:user_id) { '12347' }
  let(:proxy) { CampusSolutions::DelegateStudents.new(fake: true, user_id: user_id) }
  subject { proxy.get }

  it_should_behave_like 'a simple proxy that returns errors'
  it_should_behave_like 'a proxy that got data successfully'
  it_should_behave_like 'a proxy that properly observes the delegated access feature flag'

  it 'returns expected mock data' do
    students = subject[:feed][:students]
    expect(students).to have(2).items
    tom = students.find {|s| s[:fullName] == 'Tom Tulliver' }
    expect(tom[:campusSolutionsId]).to eq '16777216'
    expect(tom[:privileges]).to eq({
      financial: false,
      viewEnrollments: false,
      viewGrades: false,
      phone: true
    })
    maggie = students.find {|s| s[:fullName] == 'Maggie Tulliver' }
    expect(maggie[:campusSolutionsId]).to eq '1073741824'
    expect(maggie[:privileges]).to eq({
      financial: true,
      viewEnrollments: false,
      viewGrades: false,
      phone: true
    })
  end
end
