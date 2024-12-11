package com.pig4cloud.pig.books.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.books.entity.BooksEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BooksMapper extends BaseMapper<BooksEntity> {

}
