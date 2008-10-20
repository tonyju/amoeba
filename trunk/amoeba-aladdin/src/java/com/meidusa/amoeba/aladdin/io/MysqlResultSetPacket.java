package com.meidusa.amoeba.aladdin.io;

import java.util.ArrayList;
import java.util.List;

import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.net.packet.PacketBuffer;

/**
 * 
 * @author struct
 *
 */
public class MysqlResultSetPacket extends ErrorResultPacket {
	
	public ResultSetHeaderPacket resulthead;
	public FieldPacket[] fieldPackets;
	public List<RowDataPacket> rowList = new ArrayList<RowDataPacket>();
	
	public MysqlResultSetPacket(String query){
		
	}
	
	public void addRowDataPacket(RowDataPacket row){
		synchronized (rowList) {
			rowList.add(row);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.meidusa.amoeba.aladdin.io.ResultPacket#wirteToConnection(com.meidusa.amoeba.net.Connection)
	 */
	public void wirteToConnection(Connection conn){
		if(isError()){
			super.wirteToConnection(conn);
			return;
		}
		
		PacketBuffer buffer = new AbstractPacketBuffer(2048);
		byte paketId = 1;
		resulthead.packetId = paketId++;
		
		//write header bytes
		appendBufferToWrite(resulthead.toByteBuffer(conn).array(),buffer,conn,false);
		
		//write fields bytes
		for(int i=0;i<fieldPackets.length;i++){
			fieldPackets[i].packetId = paketId++;
			appendBufferToWrite(fieldPackets[i].toByteBuffer(conn).array(),buffer,conn,false);
		}
		
		//write eof bytes
		EOFPacket eof = new EOFPacket();
		eof.serverStatus = 2;
		eof.warningCount = 0;
		eof.packetId = paketId++;
		appendBufferToWrite(eof.toByteBuffer(conn).array(),buffer,conn,false);
		
		if(rowList.size()>0){
			//write rows bytes
			for(RowDataPacket row : rowList){
				row.packetId = paketId++;
				appendBufferToWrite(row.toByteBuffer(conn).array(),buffer,conn,false);
			}
			
		}
		
		//write eof bytes
		eof.packetId = paketId++;
		appendBufferToWrite(eof.toByteBuffer(conn).array(),buffer,conn,true);
	}
	
	private  boolean appendBufferToWrite(byte[] byts,PacketBuffer buffer,Connection conn,boolean writeNow){
		if(writeNow || buffer.remaining() < byts.length){
			if(buffer.getPosition()>0){
				buffer.writeBytes(byts);
				conn.postMessage(buffer.toByteBuffer());
				buffer.reset();
			}else{
				conn.postMessage(byts);
			}
			return true;
		}else{
			buffer.writeBytes(byts);
			return true;
		}
	}
}