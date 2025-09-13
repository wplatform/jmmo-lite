/*
 * Copyright (c) 2009 Stephen Tu <stephen_tu@berkeley.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.azeroth.portal.rpc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.github.azeroth.net.CommonNetty;
import com.github.azeroth.net.Connection;
import com.github.azeroth.common.RpcErrorCode;
import com.github.azeroth.portal.proto.RpcPacket;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.LinkedList;

public class DefaultRpcController implements RpcController {

	private final LinkedList<RpcPacket> packets = new LinkedList<>();
	private static final AttributeKey<RpcSession> SESSION_KEY = AttributeKey.valueOf("$RPC_SESSION");

	private String reason;
	private boolean failed;
	private boolean canceled;
	private int status = RpcErrorCode.STATUS_OK;
	@SuppressWarnings("unused")
	private RpcCallback<Object> callback;


	private final Connection connection;

    public DefaultRpcController(Connection connection) {
        this.connection = connection;
    }

    public String errorText() {
		return reason;
	}

	public boolean failed() {
		return failed;
	}

	public int status() {
		return status;
	}


	public void notifyOnCancel(RpcCallback<Object> callback) {
		this.callback = callback;
	}

	public void reset() {
		reason = null;
		failed = false;
		canceled = false;
		callback = null;
	}

	@Deprecated
	public void setFailed(String reason) {
		this.status = RpcErrorCode.ERROR_RPC_SERVER_ERROR;
		this.reason = reason;
		this.failed = true;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	public void setFailed(int status) {
		this.status = status;
		this.failed = true;
	}

	public void setFailed(int status, String reason) {
		this.status = status;
		this.reason = reason;
		this.failed = true;
	}


	public void startCancel() {
		canceled = true;
	}



	//connect methods

	public InetSocketAddress remoteAddress() {
        return ((InetSocketAddress) connection.remoteAddress());
	}

	public RpcSession getSession() {
		RpcSession rpcSession = connection.channel().attr(SESSION_KEY).get();
		if (rpcSession == null) {
			rpcSession = new RpcSession();
			connection.channel().attr(SESSION_KEY).set(rpcSession);
		}
		return rpcSession;
	}

	public String format(String msg) {
		return CommonNetty.format(connection.channel(), msg);
	}

	public void offer(RpcPacket packet) {
		this.packets.offer(packet);
	}
}
