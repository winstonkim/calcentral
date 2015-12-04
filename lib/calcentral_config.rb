module CalcentralConfig
  extend self

  def load_ruby_configs
    calcentral_settings_files('environments', 'rb').each do |file|
      if file && File.exists?(file.to_s)
        require file
      end
    end
  end

  def load_settings
    deep_open_struct(load_yaml_settings)
  end

  def reload_settings
    if Kernel.const_defined? :Settings
      new_settings = load_settings
      Rails.logger.warn 'Preparing to reload application settings via Kernel'
      Kernel.const_set(:Settings, new_settings)
      update_rails_logger_level Settings.logger.level
      Rails.logger.warn 'YAML settings reloaded. Ask yourself, should I clear the cache?'
    end
  end

  def deep_open_struct(hash_recursive)
    require 'ostruct'
    obj = hash_recursive
    if obj.is_a?(Hash)
      obj.each do |key, val|
        obj[key] = deep_open_struct(val)
      end
      obj = OpenStruct.new(obj)
    elsif obj.is_a?(Array)
      obj = obj.map {|val| deep_open_struct(val)}
    end
    obj
  end

  def load_yaml_settings
    loaded_settings = {}
    calcentral_settings_files('settings', 'yml').each do |file|
      if file && File.exists?(file.to_s)
        result = YAML.load(ERB.new(IO.read(file.to_s)).result)
        if result
          loaded_settings.deep_merge!(result)
        end
      end
    end
    loaded_settings
  end

  def local_dir
    dir = ENV['CALCENTRAL_CONFIG_DIR'] || File.join(ENV['HOME'], '.calcentral_config')
    File.exists?(dir) ? File.expand_path(dir) : nil
  end

  private

  def calcentral_settings_files(standard_dir, extension)
    files = [
        Rails.root.join('config', "settings.#{extension}"),
        Rails.root.join('config', standard_dir, "#{Rails.env}.#{extension}"),
        Rails.root.join('config', "settings.local.#{extension}"),
        Rails.root.join('config', standard_dir, "#{Rails.env}.local.#{extension}")
    ]
    if local_dir
      files.push(
          File.join(local_dir, "settings.local.#{extension}"),
          File.join(local_dir, "#{Rails.env}.local.#{extension}")
      )
    end
    files
  end

  def update_rails_logger_level(new_level)
    if invalid_logger_level? new_level
      Rails.logger.warn "The new logger level is invalid: #{new_level}"
    else
      Rails.logger.outputters.each do |outputter|
        old_level = outputter.level
        if old_level == new_level
          Rails.logger.warn "No change to level on logger output #{outputter.name}: #{Log4r::LNAMES[old_level]}"
        else
          outputter.level = new_level
          Rails.logger.warn "Logger output #{outputter.name} level changed (old -> new): #{Log4r::LNAMES[old_level]} -> #{Log4r::LNAMES[outputter.level]}"
        end
      end
    end
  end

  def invalid_logger_level?(level)
    (0...Log4r::LNAMES.length).to_a.exclude? level
  end

end
