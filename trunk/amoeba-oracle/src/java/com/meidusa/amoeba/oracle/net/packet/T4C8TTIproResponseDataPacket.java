package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * Э�����ݰ�
 * 
 * @author hexianmao
 * @version 2008-8-14 ����07:29:53
 */
public class T4C8TTIproResponseDataPacket extends T4CTTIMsgPacket {

    byte           proSvrVer        = 6;
    short          oVersion         = -1;
    String         proSvrStr        = "Linuxi386/Linux-2.0.34-8.1.0";
    short          svrCharSet       = 0;
    byte           svrFlags         = 1;
    short          svrCharSetElem   = 0;
    boolean        svrInfoAvailable = false;
    short          NCHAR_CHARSET    = 0;

    private int    i                = 0;
    private byte[] abyte0           = null;
    private short  word0            = 0;
    private byte[] as0              = null;
    private short  word1            = 0;
    private byte[] as1              = null;

    public T4C8TTIproResponseDataPacket(){
        this.msgCode = TTIPRO;
    }

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("Υ��Э��");
        }
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;
        proSvrVer = buffer.unmarshalSB1();
        switch (proSvrVer) {
            case 4:
                oVersion = MIN_OVERSION_SUPPORTED;
                break;
            case 5:
                oVersion = ORACLE8_PROD_VERSION;
                break;
            case 6:
                oVersion = ORACLE81_PROD_VERSION;
                break;
            default:
                throw new RuntimeException("��֧�ִӷ��������յ��� TTC Э��汾");
        }
        buffer.unmarshalSB1();
        proSvrStr = new String(buffer.unmarshalTEXT(50));
        svrCharSet = (short) buffer.unmarshalUB2();
        svrFlags = (byte) buffer.unmarshalUB1();
        svrCharSetElem = (short) buffer.unmarshalUB2();
        if (svrCharSetElem > 0) {
            buffer.unmarshalNBytes(svrCharSetElem * 5);
        }
        svrInfoAvailable = true;

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = buffer.getRep((byte) 1);
        buffer.setRep((byte) 1, (byte) 0);
        i = buffer.unmarshalUB2();
        buffer.setRep((byte) 1, byte0);
        abyte0 = buffer.unmarshalNBytes(i);
        int j = 6 + (abyte0[5] & 0xff) + (abyte0[6] & 0xff);
        NCHAR_CHARSET = (short) ((abyte0[j + 3] & 0xff) << 8);
        NCHAR_CHARSET |= (short) (abyte0[j + 4] & 0xff);

        if (proSvrVer < 6) {
            return;
        }
        word0 = buffer.unmarshalUB1();
        as0 = new byte[word0];
        for (int k = 0; k < word0; k++) {
            as0[k] = (byte) buffer.unmarshalUB1();
        }
        word1 = buffer.unmarshalUB1();
        as1 = new byte[word1];
        for (int l = 0; l < word1; l++) {
            as1[l] = (byte) buffer.unmarshalUB1();
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeByte(proSvrVer);
        meg.marshalNULLPTR();
        meg.writeBytes(proSvrStr.getBytes());
        meg.marshalNULLPTR();
        meg.marshalUB2(svrCharSet);
        meg.marshalUB1(svrFlags);
        meg.marshalUB2(svrCharSetElem);
        if (svrCharSetElem > 0) {
            byte[] ab = new byte[svrCharSetElem * 5];
            meg.marshalB1Array(ab);
        }

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = meg.getRep((byte) 1);
        meg.setRep((byte) 1, (byte) 0);
        meg.marshalUB2(i);
        meg.setRep((byte) 1, byte0);
        meg.marshalB1Array(abyte0);

        if (proSvrVer < 6) {
            return;
        }
        meg.marshalUB1(word0);
        meg.marshalB1Array(as0);
        meg.marshalUB1(word1);
        meg.marshalB1Array(as1);
    }

}