# aws-s3-example
Simple (crappy) example of uploading files to an s3 bucket

This was created to test some issues I have been having with file transfer times to an S3 bucket.
My suspicion was these were down to a Hibernate issue in the project in question, not an S3 or filesize issue.
Some basic (average-ish) timings taken suggest that on my connection (around 9.4Mb/s upload) each 1Mb transferred takes about 1s.
