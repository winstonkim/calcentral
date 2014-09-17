require 'spec_helper'

describe MyAcademics::Semesters do

  context 'when using Peoplesoft demo API' do
    subject { MyAcademics::Semesters.new("300939").merge(@feed ||= {}); @feed[:semesters] }

    it 'should have some expected data' do
      expect(subject).to be
      expect(subject[0][:name]).to eq 'Fall 2014'
      expect(subject[0][:termCode]).to eq 'D'
      expect(subject[0][:termYear]).to eq '2014'
      expect(subject[0][:timeBucket]).to eq 'current'
      expect(subject[0][:slug]).to eq 'fall-2014'
      expect(subject[0][:classes].length).to eq 3
      expect(subject[0][:classes][0][:course_code]).to eq 'PHILO 372'
      expect(subject[0][:classes][0][:dept]).to eq 'PHILO'
      expect(subject[0][:classes][0][:courseCatalog]).to eq '372'
      expect(subject[0][:classes][0][:dept_desc]).to eq 'Philosophy'
      expect(subject[0][:classes][0][:slug]).to eq 'philo-372'
      expect(subject[0][:classes][0][:course_id]).to eq 'philo-372-2014-D'
      expect(subject[0][:classes][0][:url]).to eq '/academics/semester/fall-2014/class/philo-372'
      expect(subject[0][:classes][0][:title]).to eq 'Contemporary Moral Issues'
      expect(subject[0][:classes][0][:sections].length).to eq 1
      expect(subject[0][:classes][0][:sections][0][:ccn]).to eq 1571
      expect(subject[0][:classes][0][:sections][0][:instruction_format]).to eq 'Laboratory'
      expect(subject[0][:classes][0][:sections][0][:is_primary_section]).to eq 'F'
      expect(subject[0][:classes][0][:sections][0][:section_label]).to eq 'LAB 1L'
      expect(subject[0][:classes][0][:sections][0][:pnp_flag]).to eq 'N'
      expect(subject[0][:classes][0][:sections][0][:gradeOption]).to eq 'Letter'
      expect(subject[0][:classes][0][:sections][0][:section_number]).to eq '1L'
      expect(subject[0][:classes][0][:sections][0][:units]).to eq 4
      expect(subject[0][:classes][0][:sections][0][:cred_cd]).to eq ''
      expect(subject[0][:classes][0][:sections][0][:schedules].length).to eq 3

      expect(subject[0][:classes][0][:sections][0][:schedules][0][:buildingName]).to eq 'Lindley Ha'
      expect(subject[0][:classes][0][:sections][0][:schedules][0][:roomNumber]).to eq '100'
      expect(subject[0][:classes][0][:sections][0][:schedules][0][:schedule]).to eq 'TuTh 11:00:00-12:20:00'

      expect(subject[0][:classes][0][:sections][0][:instructors].length).to eq 1
      expect(subject[0][:classes][0][:sections][0][:instructors][0][:name]).to eq 'TBA'
    end

  end

  context 'when using fake Oracle MV', ignore: true, if: CampusOracle::Queries.test_data? do
    subject { MyAcademics::Semesters.new("300939").merge(@feed ||= {}); @feed[:semesters] }

    describe "should get properly formatted data" do
      it { subject.length.should eq(4) }
      it { subject[0][:name].should eq "Summer 2014" }
      it { subject[0][:termCode].should eq "C" }
      it { subject[0][:termYear].should eq "2014" }
      it { subject[0][:timeBucket].should eq 'future' }
      it { subject[0][:classes].length.should eq 1 }
      it { subject[0][:classes][0][:course_code].should eq "BIOLOGY 1A" }
      it { subject[0][:classes][0][:dept].should eq "BIOLOGY" }
      it { subject[0][:classes][0][:sections].length.should eq 1 }
      it { subject[0][:classes][0][:sections][0][:ccn].should eq "07309" }
      it { subject[0][:classes][0][:sections][0][:waitlistPosition].should eq 42 }
      it { subject[0][:classes][0][:sections][0][:enroll_limit].should eq 5000 }
      it { subject[0][:classes][0][:sections][0][:gradeOption].should eq "P/NP" }
      it { subject[0][:classes][0][:url].should eq '/academics/semester/summer-2014/class/biology-1a' }
      it { subject[1][:name].should eq "Spring 2014" }
      it { subject[1][:timeBucket].should eq 'future' }
      it { subject[2][:name].should eq "Fall 2013" }
      it { subject[2][:timeBucket].should eq 'current' }
      it { subject[3][:name].should eq "Spring 2012" }
      it { subject[3][:timeBucket].should eq 'past' }
      it { subject[2][:classes].length.should eq 2 }
      it { subject[2][:classes][0][:course_code].should eq "BIOLOGY 1A" }
      it { subject[2][:classes][0][:dept].should eq "BIOLOGY" }
      it { subject[2][:classes][0][:sections].length.should eq 2 }
      it { subject[2][:classes][0][:sections][0][:ccn].should eq "07309" }
      it { subject[2][:classes][0][:sections][0][:schedules][0][:schedule].should eq "M 4:00P-5:00P" }
      it { subject[2][:classes][0][:slug].should eq "biology-1a" }
      it { subject[2][:classes][0][:title].should eq "General Biology Lecture" }
      it { subject[2][:classes][0][:url].should eq '/academics/semester/fall-2013/class/biology-1a' }
      it { subject[2][:classes][0][:sections][0][:gradeOption].should eq "Letter" }
      it { subject[2][:classes][0][:sections][0][:instruction_format].should eq "LEC" }
      it { subject[2][:classes][0][:sections][0][:section_number].should eq "003" }
      it { subject[2][:classes][0][:sections][0][:section_label].should eq "LEC 003" }
      it { subject[2][:classes][0][:sections][0][:instructors][0][:name].present?.should be_true }
      it { subject[2][:classes][0][:sections][0][:is_primary_section].should be_true }
      it { subject[2][:classes][0][:sections][0][:units].to_s.should eq "5.0" }
      it { subject[3][:classes][0][:transcript][0][:grade].should eq "B" }
      it { subject[3][:classes][0][:transcript][0][:units].to_s.should eq "4.0" }
      it { subject[3][:classes][1][:transcript][0][:grade].should eq "C+" }
      it { subject[3][:classes][1][:transcript][0][:units].to_s.should eq "3.0" }
    end

    context 'with constrained semester range' do
      before { Settings.terms.stub(:oldest).and_return('fall-2013') }
      its(:length) { should eq 3 }
    end
  end

  describe 'grading_in_progress', ignore: true do
    let(:uid) { rand(99999) }
    before { allow(Settings.terms).to receive(:fake_now).and_return(fake_now) }
    subject { MyAcademics::Semesters.new(uid).semester_info(2014, 'B')[:gradingInProgress] }
    context 'past semester' do
      let(:fake_now) { DateTime.parse('2014-06-10') }
      it { should be_nil }
    end
    context 'semester just ended' do
      let(:fake_now) { DateTime.parse('2014-05-30') }
      it { should be_true }
    end
    context 'current semester' do
      let(:fake_now) { DateTime.parse('2014-05-10') }
      it { should be_nil }
    end
  end

end
