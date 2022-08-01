package cerberus.core.persistence.impl;

import javax.persistence.EntityManager;

import cerberus.core.persistence.entities.PingInfo;

public class PingInfoDao extends AbstractDao<PingInfo, Long> {

	private static final long serialVersionUID = 98123890489102341L;
	
	public PingInfoDao() {
	}
	
	public PingInfoDao(EntityManager em, Class<PingInfo> clazz) {
		super(em, clazz);
	}
}
