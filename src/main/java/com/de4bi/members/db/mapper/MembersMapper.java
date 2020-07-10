package com.de4bi.members.db.mapper;

import com.de4bi.members.data.dao.MembersDao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MembersMapper {
    
    // Insert
    public void insert(MembersDao insertMembersDao);
    
    // Select
    public MembersDao select(long seq);
    public MembersDao selectById(String id);

    // Update
    public MembersDao update(MembersDao updateMembersDao);

    // Delete
    public void delete(long seq);
}