import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
import org.apache.commons.io.FileUtils;

public class AmazonS3Example {

    private static final String SUFFIX = "/";
    private static final String bucketName = "testbucket-dna";
    private static final String folderName = "testFolder";
    private static final String fileName = "test30MbFile";
    private static final String localFileLocation = "C:\\data\\RoyalsWin.zip";

    public static void main(String[] args) throws Exception {
        // credentials object identifying user for authentication
        // user must have AWSConnector and AmazonS3FullAccess for
        // this example to work
        AWSCredentials credentials = new ProfileCredentialsProvider("dna").getCredentials();

        // create a client connection based on credentials
        AmazonS3Client s3client = new AmazonS3Client(credentials);

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

        System.out.println("Beginning file transfers (rough guide, 1s per Mb)...");

        String timeElapsedMillis1 = saveOne(s3client, s3FileName + "1");
        System.out.println("File Transfer (method 1) took " + timeElapsedMillis1 + "ms");

        String timeElapsedMillis2 = saveTwo(s3client, s3FileName + "2");
        System.out.println("File Transfer (method 2) took " + timeElapsedMillis2 + "ms");

//        deleteFolder(bucketName, folderName, s3client);

        // deletes bucket
//        s3client.deleteBucket(bucketName);
    }

    public static String saveOne(AmazonS3Client s3Client, String s3FileName) {
        long startTime = System.currentTimeMillis();
        s3Client.putObject(new PutObjectRequest(bucketName, s3FileName,
                new File(localFileLocation)));
        long endTime = System.currentTimeMillis();

        return "" + (endTime - startTime);
    }

    public static String saveTwo(AmazonS3Client s3Client, String s3FileName) throws IOException {
        //keep the conversion outside the timing section
        byte[] bytes = FileUtils.readFileToByteArray(new File(localFileLocation));

        long startTime = System.currentTimeMillis();
        s3Client.putObject(new PutObjectRequest(bucketName, s3FileName, new ByteArrayInputStream(bytes), new ObjectMetadata()));
        long endTime = System.currentTimeMillis();

        return "" + (endTime - startTime);
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