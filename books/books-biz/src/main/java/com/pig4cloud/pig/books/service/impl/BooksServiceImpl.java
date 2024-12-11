package com.pig4cloud.pig.books.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.books.entity.BooksEntity;
import com.pig4cloud.pig.books.mapper.BooksMapper;
import com.pig4cloud.pig.books.service.BooksService;
import org.springframework.stereotype.Service;

/**
 * 图书馆图书信息表
 *
 * @author pumpkin
 * @date 2024-12-11 10:13:51
 */
@Service
public class BooksServiceImpl extends ServiceImpl<BooksMapper, BooksEntity> implements BooksService {

}
