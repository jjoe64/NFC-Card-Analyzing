package com.jjoe64.nfccardhack;

import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
		(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
		(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
		(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

	public static String getHexString(byte[] raw, int len) {
		byte[] hex = new byte[2 * len];
		int index = 0;
		int pos = 0;

		for (byte b : raw) {
			if (pos >= len)
				break;

			pos++;
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		return new String(hex);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		parse(getIntent());
	}

	private void parse(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.i("TagView", "xxxx="+ Arrays.toString(tag.getTechList()));

			MifareClassic mfc = MifareClassic.get(tag);
			byte[] data;

			try {       //  5.1) Connect to card
				mfc.connect();
				boolean auth = false;
				// 5.2) and get the number of sectors this card has..and loop thru these sectors
				int secCount = mfc.getSectorCount();
				int bCount = 0;
				int bIndex = 0;
				for (int j = 0; j < secCount; j++) {
					// 6.1) authenticate the sector
					auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
					if(auth){
						// 6.2) In each sector - get the block count
						bCount = mfc.getBlockCountInSector(j);
						bIndex = 0;
						for(int i = 0; i < bCount; i++){
							bIndex = mfc.sectorToBlock(j);
							// 6.3) Read the block
							data = mfc.readBlock(bIndex);
							// 7) Convert the data into a string from Hex format.
							Log.i("MainActivity", "*** read data="+getHexString(data, data.length));
							bIndex++;
						}
					}else{ // Authentication failed - Handle it
						Log.e("MainActivity", "*** nfc auth error ***");
					}
				}
			}catch (IOException e) {
				Log.e("MainActivity", e.getLocalizedMessage());
			}

		} else {
			Log.e("MainActivity", "Unknown intent " + intent);
			return;
		}
		finish();
	}
}