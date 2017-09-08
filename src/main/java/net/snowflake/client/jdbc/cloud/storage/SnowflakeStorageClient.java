/*
 * Copyright (c) 2017 Snowflake Computing Inc. All right reserved.
 */
package net.snowflake.client.jdbc.cloud.storage;

import net.snowflake.client.core.SFSession;
import net.snowflake.client.jdbc.FileBackedOutputStream;
import net.snowflake.client.jdbc.SnowflakeSQLException;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Interface for storage client provider implementations
 *
 * @author lgiakoumakis
 */
public interface SnowflakeStorageClient
{
  /**
   * @return Returns the Max number of retry attempts
   */
  int getMaxRetries();

  /**
   * Returns the max exponent for multiplying backoff with the power of 2, the value
   * of 4 will give us 16secs as the max number of time to sleep before retry
   *
   * @return Returns the exponent
   */
  int getRetryBackoffMaxExponent();

  /**
   * @return  Returns the min number of milliseconds to sleep before retry
   */
   int getRetryBackoffMin();

  /**
   * @return Returns true if encryption is enabled
   */
  boolean isEncrypting();

  /**
   * @return Returns the size of the encryption key
   */
  int getEncryptionKeySize();

  /** Re-creates the encapsulated storage client with a fresh access token
   * @param stageCredentials a Map (as returned by GS) which contains the new credential properties
   * @throws SnowflakeSQLException
   **/
  void renew(Map stageCredentials) throws SnowflakeSQLException;

  /**
   *   shuts down the client
   */
  void shutdown();

  /**
   * For a set of remote storage objects under a remote location and a given prefix/path
   * returns their properties wrapped in ObjectSummary objects
   * @param remoteStorageLocation location, i.e. bucket for S3
   * @param prefix the prefix to list
   * @return a collection of storage summary objects
   * @throws StorageProviderException
   */
  StorageObjectSummaryCollection listObjects(String remoteStorageLocation, String prefix)
                                            throws StorageProviderException;

  /**
   * Returns the metadata properties for a remote storage object
   * @param remoteStorageLocation location, i.e. bucket for S3
   * @param prefix the prefix/path of the object to retrieve
   * @return storage metadata object
   * @throws StorageProviderException
   */
  StorageObjectMetadata getObjectMetadata(String remoteStorageLocation, String prefix)
                                         throws StorageProviderException;

  /**
   * Download a file from remote storage.
   * @param connection connection object
   * @param command command to download file
   * @param localLocation local file path
   * @param destFileName destination file name
   * @param parallelism number of threads for parallel downloading
   * @param remoteStorageLocation remote storage location, i.e. bucket for S3
   * @param stageFilePath stage file path
   * @param stageRegion region name where the stage persists
   * @throws SnowflakeSQLException
   **/
void download(SFSession connection, String command, String localLocation, String destFileName,
                int parallelism, String remoteStorageLocation, String stageFilePath, String stageRegion)
                throws SnowflakeSQLException;

  /**
   * Upload a file (-stream) to remote storage
   * @param connection connection object
   * @param command upload command
   * @param parallelism number of threads do parallel uploading
   * @param uploadFromStream true if upload source is stream
   * @param remoteStorageLocation s3 bucket name
   * @param srcFile source file if not uploading from a stream
   * @param destFileName file name on remote storage after upload
   * @param inputStream stream used for uploading if fileBackedOutputStream is null
   * @param fileBackedOutputStream stream used for uploading if not null
   * @param meta object meta data
   * @param stageRegion region name where the stage persists
   * @throws SnowflakeSQLException if upload failed even after retry
   */
  void upload(SFSession connection, String command, int parallelism, boolean uploadFromStream,
              String remoteStorageLocation, File srcFile, String destFileName, InputStream inputStream,
              FileBackedOutputStream fileBackedOutputStream, StorageObjectMetadata meta, String stageRegion)
                throws SnowflakeSQLException;
  /**
   * Handles exceptions thrown by the remote storage provider
   * @param ex the exception to handle
   * @param retryCount current number of retries, incremented by the caller before each call
   * @param operation string that indicates the function/operation that was taking place,
   *                  when the exception was raised, for example "upload"
   * @param connection the current SFSession object used by the client
   * @param command the command attempted at the time of the exception
   * @throws SnowflakeSQLException
   */
  void handleStorageException(Exception ex, int retryCount, String operation, SFSession connection, String command)
          throws SnowflakeSQLException;


}