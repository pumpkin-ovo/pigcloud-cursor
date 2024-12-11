package com.pig4cloud.pig.books.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.pig.books.entity.BooksEntity;
import com.pig4cloud.pig.books.service.BooksService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 图书馆图书信息表
 *
 * @author pumpkin
 * @date 2024-12-11 10:13:51
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/books" )
@Tag(description = "books" , name = "图书馆图书信息表管理" )
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class BooksController {

    private final  BooksService booksService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param books 图书馆图书信息表
     * @return
     */
    @Operation(summary = "分页查询" , description = "分页查询" )
    @GetMapping("/page" )
    @HasPermission("books_books_view")
    public R getBooksPage(@ParameterObject Page page, @ParameterObject BooksEntity books) {
        LambdaQueryWrapper<BooksEntity> wrapper = Wrappers.lambdaQuery();
        return R.ok(booksService.page(page, wrapper));
    }


    /**
     * 通过条件查询图书馆图书信息表
     * @param books 查询条件
     * @return R  对象列表
     */
    @Operation(summary = "通过条件查询" , description = "通过条件查询对象" )
    @GetMapping("/details" )
    @HasPermission("books_books_view")
    public R getDetails(@ParameterObject BooksEntity books) {
        return R.ok(booksService.list(Wrappers.query(books)));
    }

    /**
     * 新增图书馆图书信息表
     * @param books 图书馆图书信息表
     * @return R
     */
    @Operation(summary = "新增图书馆图书信息表" , description = "新增图书馆图书信息表" )
    @SysLog("新增图书馆图书信息表" )
    @PostMapping
    @HasPermission("books_books_add")
    public R save(@RequestBody BooksEntity books) {
        return R.ok(booksService.save(books));
    }

    /**
     * 修改图书馆图书信息表
     * @param books 图书馆图书信息表
     * @return R
     */
    @Operation(summary = "修改图书馆图书信息表" , description = "修改图书馆图书信息表" )
    @SysLog("修改图书馆图书信息表" )
    @PutMapping
    @HasPermission("books_books_edit")
    public R updateById(@RequestBody BooksEntity books) {
        return R.ok(booksService.updateById(books));
    }

    /**
     * 通过id删除图书馆图书信息表
     * @param ids bookId列表
     * @return R
     */
    @Operation(summary = "通过id删除图书馆图书信息表" , description = "通过id删除图书馆图书信息表" )
    @SysLog("通过id删除图书馆图书信息表" )
    @DeleteMapping
    @HasPermission("books_books_del")
    public R removeById(@RequestBody Integer[] ids) {
        return R.ok(booksService.removeBatchByIds(CollUtil.toList(ids)));
    }


    /**
     * 导出excel 表格
     * @param books 查询条件
   	 * @param ids 导出指定ID
     * @return excel 文件流
     */
    @ResponseExcel
    @GetMapping("/export")
    @HasPermission("books_books_export")
    public List<BooksEntity> exportExcel(BooksEntity books,Integer[] ids) {
        return booksService.list(Wrappers.lambdaQuery(books).in(ArrayUtil.isNotEmpty(ids), BooksEntity::getBookId, ids));
    }

    /**
     * 导入excel 表
     * @param booksList 对象实体列表
     * @param bindingResult 错误信息列表
     * @return ok fail
     */
    @PostMapping("/import")
    @HasPermission("books_books_export")
    public R importExcel(@RequestExcel List<BooksEntity> booksList, BindingResult bindingResult) {
        return R.ok(booksService.saveBatch(booksList));
    }
}
