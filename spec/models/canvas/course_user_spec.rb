describe Canvas::CourseUser do

  let(:user_id)         { 4321321 }
  let(:course_id)       { 767330 }
  let(:canvas_course_user) do
    {
      'id' => 4321321,
      'name' => 'Michael Steven OWEN',
      'sis_user_id' => 'UID:105431',
      'sis_login_id' => '105431',
      'login_id' => '105431',
      'enrollments' => [
        {'course_id' => 767330, 'course_section_id' => 1312468, 'id' => 20241907, 'type' => 'StudentEnrollment', 'role' => 'StudentEnrollment'},
        {'course_id' => 767330, 'course_section_id' => 1312468, 'id' => 20241908, 'type' => 'ObserverEnrollment', 'role' => 'ObserverEnrollment'},
      ]
    }
  end

  subject { Canvas::CourseUser.new(:user_id => user_id, :course_id => course_id) }

  context 'when initializing' do
    it 'raises exception if user id option not present' do
      expect { Canvas::CourseUser.new(:course_id => course_id) }.to raise_error(ArgumentError, 'User ID option required')
    end

    it 'raises exception if user id option is not an integer' do
      expect { Canvas::CourseUser.new(:user_id => '#{user_id}', :course_id => course_id) }.to raise_error(ArgumentError, 'User ID option must be a Fixnum')
    end

    it 'raises exception if course id option not present' do
      expect { Canvas::CourseUser.new(:user_id => user_id) }.to raise_error(ArgumentError, 'Course ID option required')
    end

    it 'raises exception if course id option is not an integer' do
      expect { Canvas::CourseUser.new(:user_id => user_id, :course_id => '#{course_id}') }.to raise_error(ArgumentError, 'Course ID option must be a Fixnum')
    end
  end

  context 'when requesting single course user from canvas' do
    context 'if course user exists in canvas' do
      it 'returns course user hash' do
        user = subject.course_user
        expect(user['id']).to eq 4321321
        expect(user['name']).to eq 'Michael Steven OWEN'
        expect(user['sis_user_id']).to eq 'UID:105431'
        expect(user['sis_login_id']).to eq '105431'
        expect(user['login_id']).to eq '105431'
        expect(user['enrollments'].count).to eq 1
        expect(user['enrollments'][0]['course_id']).to eq 767330
        expect(user['enrollments'][0]['course_section_id']).to eq 1312468
        expect(user['enrollments'][0]['id']).to eq 20241907
        expect(user['enrollments'][0]['type']).to eq 'StudentEnrollment'
        expect(user['enrollments'][0]['role']).to eq 'StudentEnrollment'
      end

      it 'uses cache by default' do
        expect(Canvas::CourseUser).to receive(:fetch_from_cache).and_return({statusCode: 200, body: {cached: 'hash'}})
        user = subject.course_user
        expect(user).to include(cached: 'hash')
      end

      it 'bypasses cache when cache option is false' do
        expect(Canvas::CourseUser).to_not receive(:fetch_from_cache)
        user = subject.course_user(:cache => false)
        expect(user['id']).to eq 4321321
      end

      context 'on request failure' do
        let(:failing_request) { {method: :get} }
        let(:response) { subject.user_response }
        it_should_behave_like 'an unpaged Canvas proxy handling request failure'
      end
    end

    context 'if course user does not exist in canvas' do
      before { expect_any_instance_of(Canvas::CourseUser).to receive(:request_internal).and_return(nil) }
      it 'returns nil' do
        user = subject.course_user
        expect(user).to be_nil
      end
    end
  end

  context 'when checking if user is course admin' do
    context 'if canvas user argument is blank' do
      it 'returns false' do
        expect(subject.class.is_course_admin?(nil)).to eq false
      end
    end

    context 'if canvas user has no matching admin role' do
      it 'returns false' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq false
      end
    end

    context 'if canvas user has teacher role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'TeacherEnrollment' }
      it 'returns true' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq true
      end
    end

    context 'if canvas user has teacher assistant role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'TaEnrollment' }
      it 'returns true' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq true
      end
    end

    context 'if canvas user has designer role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'DesignerEnrollment' }
      it 'returns true' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq true
      end
    end

    context 'if canvas user has owner role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'Owner' }
      it 'returns true' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq true
      end
    end

    context 'if canvas user has maintainer role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'Maintainer' }
      it 'returns true' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq true
      end
    end

    context 'if canvas user has member role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'Member' }
      it 'returns false' do
        expect(subject.class.is_course_admin?(canvas_course_user)).to eq false
      end
    end
  end

  context 'when checking if user is course teacher' do
    context 'if canvas user argument is blank' do
      it 'returns false' do
        expect(subject.class.is_course_teacher?(nil)).to be_falsey
      end
    end

    context 'if canvas user has teacher role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'TeacherEnrollment' }
      it 'returns true' do
        expect(subject.class.is_course_teacher?(canvas_course_user)).to be_truthy
      end
    end
  end

  context 'when checking if user is course teachers assistant' do
    context 'if canvas user argument is blank' do
      it 'returns false' do
        expect(subject.class.is_course_teachers_assistant?(nil)).to be_falsey
      end
    end

    context 'if canvas user has teacher role' do
      before { canvas_course_user['enrollments'][1]['role'] = 'TaEnrollment' }
      it 'returns true' do
        expect(subject.class.is_course_teachers_assistant?(canvas_course_user)).to be_truthy
      end
    end
  end

end
