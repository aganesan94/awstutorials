package org.aganesan.waf.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.waf.AWSWAF;
import com.amazonaws.services.waf.AWSWAFClient;
import com.amazonaws.services.waf.model.ChangeAction;
import com.amazonaws.services.waf.model.CreateIPSetRequest;
import com.amazonaws.services.waf.model.CreateIPSetResult;
import com.amazonaws.services.waf.model.GetChangeTokenRequest;
import com.amazonaws.services.waf.model.GetChangeTokenResult;
import com.amazonaws.services.waf.model.GetIPSetRequest;
import com.amazonaws.services.waf.model.GetIPSetResult;
import com.amazonaws.services.waf.model.IPSet;
import com.amazonaws.services.waf.model.IPSetDescriptor;
import com.amazonaws.services.waf.model.IPSetDescriptorType;
import com.amazonaws.services.waf.model.IPSetUpdate;
import com.amazonaws.services.waf.model.UpdateIPSetRequest;

/**
 * This API is used to onboard IPRestrictions to WAF set up.
 * 
 * @author AGanesan
 *
 */
public class OnBoardIPRestrictionsToAWS {

	private static final String IP_SET_ID = "3c8d3d3f-19c6-42c3-a58d-bbba9d568a6b";
	private static final String CIDR_FORMATTED_IP = "43.245.100.1/32";
	private static final String IP_RESTRICTION_NAME = "WHITE_LIST";

	public static void main(String args[]) {

		AWSCredentials awsCredentials = null;
		try {
			awsCredentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/Users/AGanesan/.aws/credentials), and is in valid format.", e);
		}

		AWSWAF client = new AWSWAFClient(awsCredentials);
		client.setRegion(Region.getRegion(Regions.US_EAST_1));
		// retrieveExistingIPRestrictions(client);
		// createIPSetRequest(client, IP_RESTRICTION_NAME);
		// deleteExistingIPRestrictions(client);
		// insertIPRestrictionsToWAF(client);
	}

	private static GetIPSetResult retrieveExistingIPRestrictions(AWSWAF client) {
		GetIPSetRequest ipSetRequest = new GetIPSetRequest();
		ipSetRequest.setIPSetId(IP_SET_ID);
		GetIPSetResult ipSetResult = client.getIPSet(ipSetRequest);
		System.out.println(ipSetResult);
		return ipSetResult;
	}

	private static void insertIPRestrictionsToWAF(AWSWAF client) {
		IPSetDescriptor ipSetDescriptor = createIPDescriptor(CIDR_FORMATTED_IP);
		Collection<IPSetDescriptor> ipSetDescriptors = new ArrayList<>();
		ipSetDescriptors.add(ipSetDescriptor);

		IPSetUpdate ipSetUpdate = new IPSetUpdate();
		ipSetUpdate.setIPSetDescriptor(ipSetDescriptor);
		ipSetUpdate.setAction(ChangeAction.INSERT);

		UpdateIPSetRequest updateIPSetRequest = new UpdateIPSetRequest();
		Collection<IPSetUpdate> updates = new ArrayList<IPSetUpdate>();
		updates.add(ipSetUpdate);
		updateIPSetRequest.setUpdates(updates);

		GetChangeTokenResult changeTokenResult = client.getChangeToken(new GetChangeTokenRequest());
		updateIPSetRequest.setChangeToken(changeTokenResult.getChangeToken());
		updateIPSetRequest.setIPSetId(IP_SET_ID);
		client.updateIPSet(updateIPSetRequest);
	}

	private static void deleteExistingIPRestrictions(AWSWAF client) {
		GetIPSetResult results = retrieveExistingIPRestrictions(client);
		List<IPSetDescriptor> ipSetDescriptor = results.getIPSet().getIPSetDescriptors();

		UpdateIPSetRequest updateIPSetRequest = new UpdateIPSetRequest();
		Collection<IPSetUpdate> updates = new ArrayList<IPSetUpdate>();

		for (IPSetDescriptor ipSetDescriptorLocal : ipSetDescriptor) {
			IPSetUpdate ipSetUpdate = new IPSetUpdate();
			ipSetUpdate.setIPSetDescriptor(ipSetDescriptorLocal);
			ipSetUpdate.setAction(ChangeAction.DELETE);
			updates.add(ipSetUpdate);
		}

		updateIPSetRequest.setUpdates(updates);

		GetChangeTokenResult changeTokenResult = client.getChangeToken(new GetChangeTokenRequest());
		updateIPSetRequest.setChangeToken(changeTokenResult.getChangeToken());
		updateIPSetRequest.setIPSetId(IP_SET_ID);
		client.updateIPSet(updateIPSetRequest);

	}

	private static IPSetDescriptor createIPDescriptor(String ipAddressInCIDRFormat) {
		IPSetDescriptor ipSetDescriptor = new IPSetDescriptor();
		ipSetDescriptor.setType(IPSetDescriptorType.IPV4);
		ipSetDescriptor.setValue(ipAddressInCIDRFormat);
		return ipSetDescriptor;
	}

	private static void createIPSetRequest(AWSWAF client, String name) {
		CreateIPSetRequest ipSetRequest = new CreateIPSetRequest();
		GetChangeTokenResult changeTokenResult = client.getChangeToken(new GetChangeTokenRequest());
		ipSetRequest.setName(name);
		ipSetRequest.setChangeToken(changeTokenResult.getChangeToken());
		CreateIPSetResult result = client.createIPSet(ipSetRequest);
		System.out.println(result);
	}

}
