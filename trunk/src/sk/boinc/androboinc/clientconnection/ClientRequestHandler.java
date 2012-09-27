/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.clientconnection;

import sk.boinc.androboinc.util.ClientId;


/**
 * Interface for client connector.
 * UI classes (Activities) use this interface to make connection to client
 * and send requests to it.
 * 
 * @see ClientReplyReceiver
 */
public interface ClientRequestHandler {

	public abstract ClientId getClientId();

	public abstract void updateClientMode(ClientReplyReceiver callback);

	public abstract void updateHostInfo(ClientReplyReceiver callback);

	public abstract void updateProjects(ClientReplyReceiver callback);

	public abstract void updateTasks(ClientReplyReceiver callback);

	public abstract void updateTransfers(ClientReplyReceiver callback);

	public abstract void updateMessages(ClientReplyReceiver callback);

	public abstract void cancelScheduledUpdates(ClientReplyReceiver callback);

	public abstract void runBenchmarks();

	public abstract void setRunMode(ClientReplyReceiver callback, int mode);

	public abstract void setNetworkMode(ClientReplyReceiver callback, int mode);

	public abstract void setGpuMode(ClientReplyReceiver callback, int mode);

	public abstract void shutdownCore();

	public abstract void doNetworkCommunication();
	
	public enum ProjectOp {
		UPDATE(1),  // RpcClient.PROJECT_UPDATE
		SUSPEND(2), // RpcClient.PROJECT_SUSPEND
		RESUME(3),  // RpcClient.PROJECT_RESUME
		NNW(4),     // RpcClient.PROJECT_NNW
		ANW(5);     // RpcClient.PROJECT_ANW
		private int mOpCode;
		ProjectOp(int opCode) {
			mOpCode = opCode;
		}
		public int opCode() {
			return mOpCode;
		}
	}
	public abstract void projectOperation(ClientReplyReceiver callback, ProjectOp operation, String projectUrl);

	public enum TaskOp {
		SUSPEND(1),  // RpcClient.RESULT_SUSPEND
		RESUME(2),   // RpcClient.RESULT_RESUME
		ABORT(3);    // RpcClient.RESULT_ABORT
		private int mOpCode;
		TaskOp(int opCode) {
			mOpCode = opCode;
		}
		public int opCode() {
			return mOpCode;
		}
	}
	public abstract void taskOperation(ClientReplyReceiver callback, TaskOp operation, String projectUrl, String taskName);

	public enum TransferOp {
		RETRY(1),  // RpcClient.TRANSFER_RETRY
		ABORT(2);  // RpcClient.TRANSFER_ABORT
		private int mOpCode;
		TransferOp(int opCode) {
			mOpCode = opCode;
		}
		public int opCode() {
			return mOpCode;
		}
	}
	public abstract void transferOperation(ClientReplyReceiver callback, TransferOp operation, String projectUrl, String fileName);
}
