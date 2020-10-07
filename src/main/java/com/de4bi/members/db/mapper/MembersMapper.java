package com.de4bi.members.db.mapper;

import com.de4bi.members.data.dao.MembersDao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MembersMapper {
    
    // Insert
    public long insert(MembersDao insertMembersDao);
    
    // Select
    public MembersDao select(long seq);
    public MembersDao selectById(String id);
    public MembersDao selectByNickname(String nickname);

    // Update
    public int update(MembersDao updateMembersDao);

    // Delete
    public int delete(long seq);
}