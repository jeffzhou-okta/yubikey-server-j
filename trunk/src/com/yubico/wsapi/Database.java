/*
 * Copyright 2008, 2009 Yubico
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 *
 * You may obtain a copy of the License at 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package com.yubico.wsapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

public class Database {
	private Connection conn;

	String user;

	String password;

	String url;

	private static Database database = null;

	static Logger log = Logger.getLogger(Database.class);

	private Database(String user, String password, String url) {
		this.user = user;
		this.password = password;
		this.url = url;
		log.debug("db=" + url);
	}

	public static Database getDefault() {
		if (database != null) {
			return database;
		}
		String s = "Database not initialized properly. Internal error.";
		throw new RuntimeException(s);
	}

	protected static void setup(String user, String password, String url)
			throws SQLException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Connection c = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			c = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			if (c != null) {
				c.close();
			}
			throw e;
		}
		database = new Database(user, password, url);
	}

	protected void disconnect() throws SQLException {
	}

	Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	private static String getClient = "select * from clients where id = ?";

	private static String getPerms = "select * from perms where id = ?";

	public Client getClient(String id) {
		try {
			conn = getConnection();

			PreparedStatement ps = conn.prepareStatement(Database.getClient);
			ps.setString(1, id);

			ResultSet rs = Database.execQuery(ps);
			if (!rs.first()) {
				log.info("No such client " + id);
				return null;
			}
			if (!rs.getBoolean("active")) {
				log.info("Client '" + id + "' is not active");
				return null;
			}

			Date created = new Date(rs.getDate("created").getTime());
			String email = rs.getString("email");
			String secret = rs.getString("secret");
			boolean active = rs.getBoolean("active");
			String perm_id = rs.getString("perm_id");

			ps = conn.prepareStatement(Database.getPerms);
			ps.setString(1, perm_id);
			rs = Database.execQuery(ps);
			if (!rs.first()) {
				log.info("No such perms " + perm_id);
				return null;
			}

			boolean voa = rs.getBoolean("verify_otp");
			boolean aca = rs.getBoolean("add_clients");
			boolean dca = rs.getBoolean("delete_clients");
			boolean aka = rs.getBoolean("add_keys");
			boolean dka = rs.getBoolean("delete_keys");

			Perms p = new Perms(voa, aca, dca, aka, dka);
			return new Client(created, active, email,
					Secret.fromBase64(secret), p);
		} catch (SQLException e) {
			log.warn(e);
			return null;
		}
	}

	private static String getPermsId = "select * from perms where "
			+ "verify_otp=? and add_clients=? and delete_clients=? "
			+ "and add_keys=? and delete_keys=?";

	public String getPermsId(Perms perms) {
		try {
			conn = getConnection();

			PreparedStatement ps = conn.prepareStatement(Database.getPermsId);
			ps.setBoolean(1, perms.getVerifyOtp());
			ps.setBoolean(2, perms.getAddClients());
			ps.setBoolean(3, perms.getDeleteClients());
			ps.setBoolean(4, perms.getAddKeys());
			ps.setBoolean(5, perms.getDeleteKeys());

			ResultSet rs = Database.execQuery(ps);
			if (!rs.first()) {
				log.info("No such perms " + perms + " in database");
				return null;
			}
			return "" + rs.getInt("id");
		} catch (SQLException e) {
			log.warn(e);
			return null;
		}
	}

	static String addPerms = "insert into perms (verify_otp, add_clients, "
			+ "delete_clients, add_keys, delete_keys) values (?,?,?,?,?)";

	public String addPerms(Perms p) throws SQLException {
		conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(Database.addPerms);

		ps.setBoolean(1, p.getVerifyOtp());
		ps.setBoolean(2, p.getAddClients());
		ps.setBoolean(3, p.getDeleteClients());
		ps.setBoolean(4, p.getAddKeys());
		ps.setBoolean(5, p.getDeleteKeys());

		int num = ps.executeUpdate();
		if (num < 1) {
			throw new SQLException("Internal error, num=" + num);
		}
		return Database.getLastInsertId(conn, "perms");
	}

	public Yubikey getYubikeyOnTokenId(byte[] id) {
		return getYubikeyOnTokenId(Crypto.toString(id));
	}

	public Yubikey getYubikeyOnTokenId(String id) {
		try {
			conn = getConnection();
			String s = "select * from yubikeys where tokenId = ?";
			PreparedStatement ps = conn.prepareStatement(s);
			ps.setString(1, id);
			log.info(ps);
			ResultSet rs = Database.execQuery(ps);
			if (!rs.first()) {
				log.info("No such yubikey " + id);
				return null;
			}
			if (!rs.getBoolean("active")) {
				log.info("Yubikey '" + id + "' is not active");
				return null;
			}
			int clientId = rs.getInt("client_id");
			Date created = new Date(rs.getDate("created").getTime());
			Date accessed = new Date(rs.getDate("accessed").getTime());
			String secret = rs.getString("secret");
			boolean active = rs.getBoolean("active");
			String tokenId = rs.getString("tokenId");
			String userId = rs.getString("userId");
			int lastCounter = rs.getInt("counter");
			int lastHi = rs.getInt("high");
			int lastLow = rs.getInt("low");
			int lastSessionUse = rs.getInt("sessionUse");
			// conn.commit();
			return new Yubikey(created, active, accessed, Secret
					.fromBase64(secret), "" + clientId, tokenId, userId,
					   lastCounter, lastHi, lastLow, lastSessionUse);
		} catch (SQLException e) {
			log.warn(e);
			return null;
		}
	}

	public void updateYubikeyOnTokenId(byte[] id, Yubikey yubikey)
			throws SQLException {
		updateYubikeyOnTokenId(Crypto.toString(id), yubikey);
	}

	static String upYubikey = "update yubikeys set accessed=?, counter=?, "
			+ "high=?, low=?, sessionUse=? where tokenId=?";

	public void updateYubikeyOnTokenId(String id, Yubikey yubikey)
			throws SQLException {
		conn = getConnection();
		PreparedStatement stmt = conn.prepareStatement(upYubikey);
		long l = yubikey.getAccessed().getTime();
		stmt.setTimestamp(1, new java.sql.Timestamp(l));
		stmt.setInt(2, yubikey.getSessionCounter());
		stmt.setInt(3, yubikey.getTimestampHigh());
		stmt.setInt(4, yubikey.getTimestampLow());
		stmt.setInt(5, yubikey.getSessionUse());
		stmt.setString(6, id);
		log.debug(stmt);
		int num = stmt.executeUpdate();
		if (num < 1) {
			throw new SQLException("Internal error, num=" + num);
		}
	}

	private static ResultSet execQuery(PreparedStatement ps)
			throws SQLException {
		log.debug(ps);
		return ps.executeQuery();
	}

	int getCount(String table) {
		try {
			conn = getConnection();
			String s = "select count(*) from " + table;
			PreparedStatement ps = conn.prepareStatement(s);

			ResultSet rs = Database.execQuery(ps);
			if (!rs.next()) {
				log.warn("No next() from result set");
				return 0;
			}
			return Integer.parseInt(rs.getString("count(*)"));
		} catch (SQLException e) {
			log.warn(e);
			return 0;
		}
	}

	static String addClient = "insert into clients (perm_id, active, created, "
			+ "email, secret) values (?,?,?,?,?)";

	public String addClient(Client c) throws SQLException {
		Perms p = c.getPerms();
		String perm_id = getPermsId(p);
		if (perm_id == null) {
			perm_id = addPerms(p);
		}

		conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(Database.addClient);

		ps.setString(1, perm_id);
		ps.setBoolean(2, c.getActive());
		ps.setTimestamp(3, new java.sql.Timestamp(c.getCreated().getTime()));
		ps.setString(4, c.getEmail());
		ps.setString(5, Crypto.toString(c.getSecret().toBytes()));

		int num = ps.executeUpdate();
		if (num < 1) {
			throw new SQLException("Internal error, num=" + num);
		}
		return Database.getLastInsertId(conn, "clients");
	}

	private static String getLastInsertId(Connection conn, String table)
			throws SQLException {
		String s = "select last_insert_id() from " + table;
		PreparedStatement ps = conn.prepareStatement(s);
		ResultSet rs = Database.execQuery(ps);
		if (!rs.next()) {
			log.warn("No next() from result set");
			throw new RuntimeException("Internal error");
		}
		return rs.getString("last_insert_id()");
	}

	static String addKey = "insert into yubikeys (client_id, active, created, "
			+ "accessed, tokenId, userId, secret) values (?,?,?,?,?,?,?)";

	public String addKey(Yubikey key) throws SQLException {
		conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(Database.addKey);

		ps.setString(1, key.getClientId());
		ps.setBoolean(2, key.getActive());
		ps.setTimestamp(3, new java.sql.Timestamp(key.getCreated().getTime()));
		ps.setTimestamp(4, new java.sql.Timestamp(key.getAccessed().getTime()));
		ps.setString(5, key.getTokenId());
		ps.setString(6, key.getUserId());
		ps.setString(7, Crypto.toString(key.getSecret().toBytes()));
		log.info(ps.toString());
		int num = ps.executeUpdate();
		if (num < 1) {
			throw new SQLException("Internal error, num=" + num);
		}
		return Database.getLastInsertId(conn, "yubikeys");
	}

	static String delKey = "update yubikeys set active=false where tokenId=?";

	public void deleteKey(Yubikey key) throws SQLException {
		conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(Database.delKey);

		ps.setString(1, key.getTokenId());
		int num = ps.executeUpdate();
		if (num < 1) {
			throw new SQLException("Internal error, num=" + num);
		}
	}
}
