package org.aganesan.elasticachetest;

import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.CacheNode;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;

import redis.clients.jedis.Jedis;

public class ElastiCacheTest {

	public static void main(String[] args) {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/AGanesan/.aws/credentials), and is in valid format.", e);
		}

		AmazonElastiCacheClient client = new AmazonElastiCacheClient(credentials);
		DescribeCacheClustersRequest dccRequest = new DescribeCacheClustersRequest();
		dccRequest.setShowCacheNodeInfo(true);

		DescribeCacheClustersResult clusterResult = client.describeCacheClusters(dccRequest);

		List<CacheCluster> cacheClusters = clusterResult.getCacheClusters();
		for (CacheCluster cacheCluster : cacheClusters) {
			for (CacheNode cacheNode : cacheCluster.getCacheNodes()) {
				String addr = cacheNode.getEndpoint().getAddress();
				int port = cacheNode.getEndpoint().getPort();
				String url =  addr + ":" + port;
				System.out.println(url);

				Jedis jedis = new Jedis(url);
				System.out.println("Connection to server sucessfully");
				// check whether server is running or not
				System.out.println("Server is running: " + jedis.ping());
			}
		}

	}

}
