class CreateServiceAlerts < ActiveRecord::Migration
  def change
    create_table :service_alerts do |t|
      t.string :title, null: false
      t.text :snippet
      t.text :body, null: false
      t.datetime :publication_date, null: false
      t.boolean :display, null: false, default: false

      t.timestamps
    end

    add_index :service_alerts, [:display, :created_at]
  end
end
