import time

import uvicorn

from BackEndTools.demo import ImageDemoSample
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import FileResponse
import os

UploadImageStoreRootPath = "sources\\username\\uploadImage"
ResultImageStoreRootPath = "sources\\username\\resImage"
app = FastAPI()

ImageProcess = ImageDemoSample("BackEndTools\\configs\\demoConfigs.yaml".replace("\\", "/"))

@app.post("/detection/resImage")
async def DetectionImage(image: UploadFile):
    # 保存文件到文件目录
    file = await image.read()
    nowTime = str(time.time())
    UploadImageStorePath = os.path.join(UploadImageStoreRootPath, nowTime + ".jpg")
    with open(UploadImageStorePath, mode="wb") as fp:
        fp.write(file)
    # 设定处理后文件的保存目录
    ResultImageStorePath = os.path.join(UploadImageStoreRootPath, nowTime + ".jpg")
    # 开始处理
    resPath = ImageProcess(UploadImageStorePath, ResultImageStorePath)
    # 将可视化后的图片返回给客户端
    return FileResponse(resPath)
