## When should I generate AES keys on my own for my Yubikeys? ##

**Convenience:** The AES you generate can be put directly into the validation server you host and be programmed into your Yubikey directly. You no longer need to ask Yubico for the AES keys of your Yubikeys.

**Privacy:** If you have the "trust no body" belief, you don't want to use the AES key generated in Yubico's manufacturing facility since that AES key also sits in Yubico's database. Then you can generate the AES keys on your own.

**Recycle:** You can re-generate the AES keys regularly if you want.

**But...:** The catch is that your self-programmed Yubikey won't be able to
authenticate against Yubico's validation server any more. That means it can't
be used to log onto sites such as Forum.yubico.com, MashedLife.com, etc.


---


## How to parse the date of the validation response from Yubico web service API? ##

SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'SSSS");


---


## How to map the database schema between the 2 PHP-based validation implementation? ##

Since there have been several implementation so far, here is a mapping between the database columns with this Java-based implementation.

[Php.1] http://code.google.com/p/yubiclass/

> Submitted by user hasterguf in forum.yubico.com

[Php.2] http://code.google.com/p/yubico-php-lib/

> Submitted by user jwoltman in forum.yubico.com

[Java.1] http://code.google.com/p/yubikey-server-j/

> Submitted by user paul in forum.yubico.com


Database column mapping across different Yubikey
validate server side implementations above


**[Java.1 -> Php.1] table 'yubikeys' -> 'yubikeytable'**
```
Column by column mapping:

(Java.1) tokenId (b64-encoded. Eg. '5B1DM1qy')

 -> (Php.1) publicID (mod-hex encoded. Eg. 'ufbtfeeeglnd')

(Java.1) secret (b64-encoded. Eg. 'gOc2J....TrdHXw==')

 -> (Php.1) AES_key (hex-encoded. Eg. '80e73625c61b....75f'

(Java.1) tokenId (b64-encoded. Eg. '5B1DM1qy')

 -> (Php.1) secret ID (hex-encoded. Eg. 'e41d43335ab2')

(Java.1) counter (int, starting from 0)

 -> (Php.1) counter (int, starting from 0)

(Java.1) created (datetime. Eg. '2008-07-03 01:53:20')
 
 -> (Php.1) tstamp (int, Eg. '1214992923')

(Java.1) accessed (datetime. Eg. '0000-00-00 00:00:00')
 
 -> (Php.1) lastEdited (timestamp. Eg. '0000-00-00 00:00:00')
```



---



**[Java.1 -> Php.2] table 'yubikeys' -> 'yubikeys'**

```
Column by column mapping:

(Java.1) tokenId (b64-encoded. Eg. '5B1DM1qy')

 -> (Php.2) yu_public_id (mod-hex encoded. Eg. 'ufbtfeeeglnd')

(Java.1) secret (b64-encoded. Eg. 'gOc2J....TrdHXw==')

 -> (Php.2) yu_aes_key (hex-encoded. Eg. '80e73625c61b....75f'

(Java.1) tokenId (b64-encoded. Eg. '5B1DM1qy')

 -> (Php.2) yu_private_id (hex-encoded. Eg. 'e41d43335ab2')

(Java.1) counter (int, starting from 0)

 -> (Php.2) yu_counter 

(Java.1) accessed (datetime. Eg. '0000-00-00 00:00:00')
 
 -> (Php.2) yu_server_timestamp

(Java.1) low, high (int, int)

 -> (Php.2) yu_timestamp
```


---