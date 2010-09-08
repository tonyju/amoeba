package com.meidusa.amoeba.mongodb.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.mongodb.handler.entry.CursorEntry;
import com.meidusa.amoeba.mongodb.io.MongodbPacketConstant;
import com.meidusa.amoeba.mongodb.net.MongodbClientConnection;
import com.meidusa.amoeba.mongodb.net.MongodbServerConnection;
import com.meidusa.amoeba.mongodb.packet.AbstractMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.KillCurosorsMongodbPacket;
import com.meidusa.amoeba.mongodb.packet.MongodbPacketBuffer;
import com.meidusa.amoeba.mongodb.packet.ResponseMongodbPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.SessionMessageHandler;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.util.Tuple;

public class CursorCloseMessageHandler implements SessionMessageHandler{
	private static Logger logger = Logger.getLogger("PACKETLOGGER");
	public Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	public MongodbClientConnection clientConn;
	public CursorCloseMessageHandler(MongodbClientConnection clientConn,Tuple<CursorEntry,ObjectPool>...tuples){
		this.clientConn = clientConn;
		for(Tuple<CursorEntry,ObjectPool> tuple : tuples){
			KillCurosorsMongodbPacket packet = new KillCurosorsMongodbPacket();
			packet.cursorIDs = new long[]{ tuple.left.cursorID};
			packet.fullCollectionName = tuple.left.fullCollectionName;
			packet.numberOfCursorIDs = 1;
			MongodbServerConnection serverConn;
			try {
				serverConn = (MongodbServerConnection)tuple.right.borrowObject();
				handlerMap.put(serverConn, serverConn.getMessageHandler());
				serverConn.setSessionMessageHandler(this);
				serverConn.postMessage(packet.toByteBuffer(serverConn));
				if(logger.isDebugEnabled()){
					logger.debug("--->>>@CursorCloseRequestPakcet="+packet+"," +clientConn.getSocketId()+" send packet --->"+serverConn.getSocketId());
				}
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void handleMessage(Connection conn, byte[] message) {
		if(logger.isDebugEnabled()){
			int type = MongodbPacketBuffer.getOPMessageType(message);
			if(type == MongodbPacketConstant.OP_REPLY){
				AbstractMongodbPacket packet = new ResponseMongodbPacket();
				packet.init(message, conn);
				logger.debug("<<<--- @CursorCloseResponsePakcet="+packet+" receive from "+conn.getSocketId()+" -->"+clientConn.getSocketId());
			}
		}
		endQuery(conn);
	}

	public void endQuery(Connection conn){
		MongodbServerConnection serverConn = (MongodbServerConnection) conn;
		serverConn.setSessionMessageHandler(null);
		serverConn.setMessageHandler(handlerMap.remove(serverConn));
		try {
			serverConn.getObjectPool().returnObject(serverConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
