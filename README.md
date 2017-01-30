# Hashable
This is a webservice which stores and serves content based on the content's hash. The "big picture" idea is that such a service could store _all of the world's content._ 

Instead of storing files and their content locally, the end user would instead upload the content and store its hash. 
Afterwards, the content could be fetched only as needed using the hash. Sharing content of arbitrary size would become 
trivial, since it's so easy to share the 512 bit hash. Effectively, all content is compressed to 512 bits and pulled 
from the service when needed.

With a cryptographically strong hash of 512 bits, there should be no concern of collisions. If the uploaded content 
has the same hash, then it must be the same content. It doesn't matter who uploaded the content since all the bits are 
the same. It should also be practically impossible to guess a valid hash without knowing the content.

If everyone were to use such a system, all unique content would only need to be stored once. 


**Disclaimer:** This is only a proof of concept.

## Outline of use
The typical use cases are as follows:

### Uploading content
1. The user submits an HTTP POST request to the service with the submission content in the body of the request.
2. The request body's bytes are converted into a SHA3 512 bit hash.
3. The request body's bytes are saved to disk.
4. The service returns HTTP status code 200 with the content's hash in the response body.

**cURL example:** `curl -X POST -d "Hello world!" http://<server_url>:<port>/`

This uploads the content "Hello world!" to the server. The server returns: 
`95DECC72F0A50AE4D9D5378E1B2252587CFC71977E43292C8F1B84648248509F1BC18BC6F0B0D0B8606A643EFF61D611AE84E6FBD4A2683165706BD6FD48B334`

### Retrieving content
1. The user submits an HTTP GET request with the URL ending in the hash
2. The server returns the stored content in the response body, or HTTP status code 404 if it's not found.

**cURL example:** `curl http://<server_url>:<port>/95DECC72F0A50AE4D9D5378E1B2252587CFC71977E43292C8F1B84648248509F1BC18BC6F0B0D0B8606A643EFF61D611AE84E6FBD4A2683165706BD6FD48B334`

This uploads the content "Hello world!" to the server. The server returns: 
`Hello world!`

## Practical considerations
No authentication is proposed in this system. The content (and their hashes) don't belong to any particular user. The
content is only a series of bits, accessible by anyone who happens to know the correct hash. There isn't a way to revoke
access to some particular content. 

Consider the scenario where a user uploads the sentence "The quick brown foxes jumps over the lazy dog." This could be 
referenced by multiple users. The original uploader has no claim to the original bits and their subsequent hash, since
anyone could have uploaded the same bits and gotten the same result. While this is straightforward in theory, it would 
likely fail in the real world due to intellectual property claims.

Additionally, this service could serve as a global (perhaps universal) content delivery network. As such, there would 
need to be replication, failover, etc. 

In reality, it seems unlikely that people would use such a system directly. 

A more practical application could be embedded in a larger service such as Amazon Web Services or Google Cloud Engine. 
If multiple users store the same content over and over, only one version needs to be stored behind the scenes. If the 
same text file (or any other content) is stored by 100 users, why keep 100 copies? The bits are the same no matter who 
is accessing them. 

## TODO
These are known issues with this POC implementation which could be improved

1. **More concurrency safety:** The service doesn't currently consider the scenario of multiple, concurrent requests.
There would be race conditions if there are requests for content the is simultaneously being uploaded. There may even 
be some consideration of race conditions when writing to disk.

2. **Performance tuning:** The service has not been optimized for performance. Some ideas would be:
    * Checking GET requests are 512 bits in length. Likely quicker than checking if the file exists on disk.
    * Storing previous hashes in a data structure that allows quick lookup. Maybe a bloom filter or database.
