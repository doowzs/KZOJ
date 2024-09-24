package top.hcode.hoj.service.file;

import top.hcode.hoj.common.exception.StatusForbiddenException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface UserFileService {

    public void generateUserExcel(String key, HttpServletResponse response) throws IOException;

    public void downloadUserList(Boolean isRLTime,HttpServletResponse response) throws StatusForbiddenException, IOException;
}
