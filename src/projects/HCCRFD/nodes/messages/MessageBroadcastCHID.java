package projects.HCCRFD.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.messages.Message;

/**
 * Message for broadcasting the CH IDs once they have been selected by the sink
 * @author edson
 *
 */
public class MessageBroadcastCHID extends Message{

	ArrayList<Integer> chids = new ArrayList<>();
	
	//ASUMPTIONS
	ArrayList<Integer> cms = new ArrayList<>();
	ArrayList<Integer> cmch = new ArrayList<>();
	
	public MessageBroadcastCHID(ArrayList<Integer> chids, ArrayList<Integer> cms, ArrayList<Integer> cmch){
		this.chids.addAll(chids);
		this.cms.addAll(cms);
		this.cmch.addAll(cmch);
	}
	
	public ArrayList<Integer> getChids(){
		return chids;
	}
	
	public ArrayList<Integer> getCms(){
		return cms;
	}
	
	public ArrayList<Integer> getCmch(){
		return cmch;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
