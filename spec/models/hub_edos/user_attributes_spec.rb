# encoding: UTF-8
describe HubEdos::UserAttributes do

  let(:user_id) { '61889' }
  let(:fake_contact_proxy) { HubEdos::Contacts.new(user_id: user_id) }
  before { allow(HubEdos::Contacts).to receive(:new).and_return fake_contact_proxy }

  let(:fake_demographics_proxy) { HubEdos::Demographics.new(user_id: user_id) }
  before { allow(HubEdos::Demographics).to receive(:new).and_return fake_demographics_proxy }

  let(:fake_affiliations_proxy) { HubEdos::Affiliations.new(user_id: user_id) }
  before { allow(HubEdos::Affiliations).to receive(:new).and_return fake_affiliations_proxy }

  subject { HubEdos::UserAttributes.new(user_id: user_id).get }

  it 'should provide the converted person data structure' do
    expect(subject[:ldap_uid]).to eq '61889'
    expect(subject[:student_id]).to eq '11667051'
    expect(subject[:first_name]).to eq 'René'
    expect(subject[:last_name]).to eq 'Bear'
    expect(subject[:person_name]).to eq 'René  Bear '
    expect(subject[:email_address]).to eq 'oski@gmail.com'
    expect(subject[:official_bmail_address]).to eq 'oski@berkeley.edu'
    expect(subject[:names]).to be
    expect(subject[:addresses]).to be
    expect(subject[:roles]).to eq({applicant: true})
  end

  context 'role transformation' do
    before do
      fake_affiliations_proxy.override_json { |json| json['studentResponse']['students']['students'][0]['affiliations'] = affiliations }
    end

    context 'undergraduate student' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'STUDENT',
              'description' => ''
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          },
          {
            'type' => {
              'code' => 'UNDERGRAD',
              'description' => 'Undergraduate Student'
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return undergraduate attributes' do
        expect(subject[:roles][:student]).to be true
        expect(subject[:ug_grad_flag]).to eq 'U'
      end
    end

    context 'graduate student' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'STUDENT',
              'description' => ''
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          },
          {
            'type' => {
              'code' => 'GRADUATE',
              'description' => 'Graduate Student'
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return graduate attributes' do
        expect(subject[:roles][:student]).to eq true
        expect(subject[:ug_grad_flag]).to eq 'G'
      end
    end

    context 'inactive student' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'STUDENT',
              'description' => ''
            },
            'statusCode' => 'INA',
            'statusDescription' => 'Inactive',
            'fromDate' => '2014-05-15'
          },
          {
            'type' => {
              'code' => 'GRADUATE',
              'description' => 'Graduate Student'
            },
            'statusCode' => 'INA',
            'statusDescription' => 'Inactive',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return ex-student attributes' do
        expect(subject[:roles][:exStudent]).to eq true
        expect(subject[:roles][:student]).to eq nil
        expect(subject[:ug_grad_flag]).to eq nil
      end
    end

    context 'former undergrad, current grad' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'STUDENT',
              'description' => ''
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          },
          {
            'type' => {
              'code' => 'GRADUATE',
              'description' => 'Graduate Student'
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          },
          {
            'type' => {
              'code' => 'UNDERGRAD',
              'description' => 'Undergraduate Student'
            },
            'statusCode' => 'INA',
            'statusDescription' => 'Inactive',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return graduate attributes' do
        expect(subject[:roles][:exStudent]).to eq nil
        expect(subject[:roles][:student]).to eq true
        expect(subject[:ug_grad_flag]).to eq 'G'
      end
    end

    context 'applicant' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'ADMT_UX',
              'description' => ''
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return applicant attributes' do
        expect(subject[:roles][:applicant]).to eq true
        expect(subject[:roles][:student]).to eq nil
      end
    end

    context 'active instructor' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'INSTRUCTOR',
              'description' => 'Instructor'
            },
            'statusCode' => 'ACT',
            'statusDescription' => 'Active',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return faculty attributes' do
        expect(subject[:roles][:faculty]).to eq true
        expect(subject[:roles][:student]).to be_nil
      end
    end

    context 'advisor affiliation' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'ADVISOR',
              'description' => 'Advisor'
            },
            'statusCode' => status_code,
            'statusDescription' => status_description,
            'fromDate' => '2014-05-15'
          }
        ]
      end
      context 'active status in date range' do
        let(:status_code) { 'ACT' }
        let(:status_description) { 'Active' }
        after do
          expect(subject[:roles]).to have(1).item
          expect(subject[:roles][:advisor]).to be true
        end
      end
      context 'inactive' do
        let(:status_code) { 'INA' }
        let(:status_description) { 'Inactive' }
        it 'should not have advisor role' do
          expect(subject[:roles]).to be_empty
        end
      end
    end

    context 'inactive instructor' do
      let(:affiliations) do
        [
          {
            'type' => {
              'code' => 'INSTRUCTOR',
              'description' => 'Instructor'
            },
            'statusCode' => 'INA',
            'statusDescription' => 'Inactive',
            'fromDate' => '2014-05-15'
          }
        ]
      end
      it 'should return no attributes' do
        expect(subject[:roles]).to eq({})
        expect(subject[:ug_grad_flag]).to be_nil
      end
    end

    context 'no affiliations' do
      let(:affiliations) { [] }
      it 'should return no attributes' do
        expect(subject[:roles]).to eq({})
        expect(subject[:ug_grad_flag]).to be_nil
      end
    end
  end

  describe '#has_role' do
    subject { HubEdos::UserAttributes.new(user_id: user_id) }
    it 'finds matching roles' do
      expect(subject.has_role?(:student, :applicant)).to be_truthy
      expect(subject.has_role?(:student)).to be_falsey
      expect(subject.has_role?(:applicant)).to be_truthy
    end
  end
end
