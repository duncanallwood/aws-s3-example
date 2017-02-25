import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonS3Example {

    private static final String SUFFIX = "/";
    private static final String bucketName = "testbucket-dna";
    private static final String folderName = "testFolder";
    private static final String fileName = "test30MbFile";
    private static final String localFileLocation = "C:\\data\\RoyalsWin.zip";

    public static void main(String[] args) {
        // credentials object identifying user for authentication
        // user must have AWSConnector and AmazonS3FullAccess for
        // this example to work
        AWSCredentials credentials = new ProfileCredentialsProvider("dna").getCredentials();

        // create a client connection based on credentials
        AmazonS3 s3client = new AmazonS3Client(credentials);

        // create bucket - name must be unique for all S3 users
        List<Bucket> buckets = s3client.listBuckets();
        boolean bucketExists = false;
        for (Bucket bucket : buckets) {
            if (bucketName.equals(bucket.getName()))
                bucketExists = true;
        }
        if (bucketExists == false)
            s3client.createBucket(bucketName);

        // list buckets
        for (Bucket bucket : s3client.listBuckets()) {
            System.out.println(" - " + bucket.getName());

        }

        // create folder into bucket

//        createFolder(bucketName, folderName, s3client);

        // upload file to folder and set it to public
        String s3FileName = folderName + SUFFIX + fileName;
        long startTime = System.currentTimeMillis();
        s3client.putObject(new PutObjectRequest(bucketName, s3FileName,
                new File(localFileLocation))
                .withCannedAcl(CannedAccessControlList.PublicRead));
        long endTime = System.currentTimeMillis();

        long timeElapsedMillis = endTime - startTime;
        System.out.println("File Transfer took " + timeElapsedMillis + "ms");

//        deleteFolder(bucketName, folderName, s3client);

        // deletes bucket
//        s3client.deleteBucket(bucketName);
    }

    public static void createFolder(String bucketName, String folderName, AmazonS3 client) {
        // create meta-data for your folder and set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        // create empty content
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        // create a PutObjectRequest passing the folder name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                folderName + SUFFIX, emptyContent, metadata);
        // send request to S3 to create folder
        client.putObject(putObjectRequest);
    }
    /**
     * This method first deletes all the files in given folder and than the
     * folder itself
     */
    public static void deleteFolder(String bucketName, String folderName, AmazonS3 client) {
        List<S3ObjectSummary> fileList = client.listObjects(bucketName, folderName).getObjectSummaries();
        for (S3ObjectSummary file : fileList) {
            client.deleteObject(bucketName, file.getKey());
        }
        client.deleteObject(bucketName, folderName);
    }
}