-- Run once before starting the updated backend. Existing text comments are preserved.
ALTER TABLE sys_hospital_comment
  ADD COLUMN audio_url varchar(500) DEFAULT NULL,
  ADD COLUMN audio_duration int DEFAULT NULL;
