# 导入必须要的库
import os
import time
import yaml
import numpy as np
from mmpose.datasets import DatasetInfo
from mmpose.apis import inference_top_down_pose_model, init_pose_model, vis_pose_result


def GetDemoConfigsFromConfigsFile(DemoConfigsFilePath: str) :
    """解析项目的yaml配置文件

    :param DemoConfigsFilePath: yaml配置文件的地址
    :return: 配置文件的键list和配置dict
    """
    # 获得项目配置文件的字节流
    FilePointer = open(DemoConfigsFilePath, encoding="utf-8")
    FileStream = FilePointer.read()
    FilePointer.close()
    # 将yaml格式的数据读取为字典格式
    DemoConfigs = yaml.safe_load(FileStream)
    # 返回键list和项目配置dict
    DemoConfigsKeyList = list(DemoConfigs.keys())
    return DemoConfigsKeyList, DemoConfigs


def LoadPoseModel(DemoConfigs: dict) :
    """加载模型

    :param DemoConfigs: 配置文件
    :return: 返回加载时间和加载的模型
    """
    # 记载加载模型的时间
    StartLoadModelTime = time.time()
    # 加载模型
    PoseModel = init_pose_model(DemoConfigs["POSE_CONFIGS_PATH"], DemoConfigs["POSE_CHECKPOINT_PATH"],
                                DemoConfigs["DEVICE"].lower())
    EndLoadPoseModelTime = time.time()
    LoadPoseModelTime = str(EndLoadPoseModelTime - StartLoadModelTime)
    # 返回加载时间和加载的模型
    return LoadPoseModelTime, PoseModel


def LoadDatasetConfigs(PoseModel: any):
    """加载对应模型的数据集信息（就是数据预处理信息）
        ！！！注意：DatasetInfos是DatasetInfo的一个实例，DatasetInfo是一个type

    :param PoseModel: 加载的模型
    :return: 数据集的名称，数据集的相关信息
    """
    Dataset = PoseModel.cfg.data["test"]["type"]
    DatasetInfos = PoseModel.cfg.data["test"].get("dataset_info", None)
    DatasetInfos = DatasetInfo(DatasetInfos)

    return Dataset, DatasetInfos


def GetPoseModelResults(PoseModel: any, InputImageFilePath: str, Dataset: str, DatasetInfos: DatasetInfo) -> list:
    """获得模型输出

    :param PoseModel:模型
    :param InputImageFilePath:输入图片的地址
    :param Dataset: 数据集名称
    :param DatasetInfos: 数据集相关信息
    :return: PoseResults => [{'bbox': array([0, 0, 0, 0],dtype=np.int32), 'keypoints': array([x,y,class], dtype=float32)}]

进程已结束,退出代码0

    """
    ReturnHeatmap, OutputLayerNames, BboxThreshold, PersonResults = False, None, None, None
    PoseResults, ReturnedOutputs = inference_top_down_pose_model(
        PoseModel,
        InputImageFilePath.replace("\\", "/"),
        bbox_thr=BboxThreshold,
        person_results=PersonResults,
        dataset=Dataset,
        dataset_info=DatasetInfos,
        return_heatmap=ReturnHeatmap,
        outputs=OutputLayerNames
    )
    PoseResults[0]["bbox"] = np.array([0, 0, 0, 0], dtype=np.int32)
    return PoseResults


def VisualizePoseModelRseultsImage(InputImageFilePath: str, OutputImageFilePath: str, PoseModel: any, PoseResults: list,
                                   Dataset: str, DatasetInfos: DatasetInfo, DemoConfigs: dict):
    """可视化图片（可选择show或者不show），并保存图片

    :param InputImageFilePath: 输入图片的地址
    :param OutputImageFilePath: 输出图片的地址
    :param PoseModel: 模型
    :param PoseResults: pose的结果
    :param Dataset: 数据集名称信息
    :param DatasetInfos: 数据集相关信息
    :param DemoConfigs: 项目配置文件
    :return: None
    """
    vis_pose_result(
        PoseModel,
        InputImageFilePath.replace("\\", "/"),
        PoseResults,
        dataset=Dataset,
        dataset_info=DatasetInfos,
        kpt_score_thr=DemoConfigs["KEYPOINT_SCORE_THRESHOLD"],
        radius=DemoConfigs["REDIUS"],
        thickness=DemoConfigs["THICKNESS"],
        show=DemoConfigs["SHOW_IMAGE"],
        out_file=OutputImageFilePath.replace("\\", "/"))


# def VisualizePoseModelRseultsVideo()


def ImageDemo(DemoConfigsFilePath: str, InputImageRootPath: str, OutputImageRootPath: str):
    # 加载项目配置
    DemoConfigsKeysList, DemoConfigs = GetDemoConfigsFromConfigsFile(DemoConfigsFilePath)
    # 加载模型
    LoadPoseModelTime, PoseModel = LoadPoseModel(DemoConfigs)
    # 加载数据预处理的相关配置
    Dataset, DatasetInfos = LoadDatasetConfigs(PoseModel)
    # 获得模型运行结果
    PoseResults = GetPoseModelResults(PoseModel, InputImageRootPath, Dataset, DatasetInfos)
    # 获得可视化结果
    VisualizePoseModelRseultsImage(InputImageRootPath, OutputImageRootPath, PoseModel, PoseResults, Dataset,
                                   DatasetInfos, DemoConfigs)


def VideoDemo():
    pass

class ImageDemoSampleClassI:

    def __init__(self,DemoConfigsFilePath):
        self.DemoConfigsFilePath = DemoConfigsFilePath
        # 加载项目配置
        self.DemoConfigsKeysList,self.DemoConfigs = GetDemoConfigsFromConfigsFile(DemoConfigsFilePath)
        # 加载模型
        self.LoadPoseModelTime, self.PoseModel = LoadPoseModel(self.DemoConfigs)
        # 加载数据预处理的相关配置
        self.Dataset, self.DatasetInfos = LoadDatasetConfigs(self.PoseModel)

    def __call__(self,InputImageRootPath,OutputImageRootPath):
        # 获得模型运行结果
        PoseResults = GetPoseModelResults(self.PoseModel, InputImageRootPath, self.Dataset, self.DatasetInfos)
        # # 获得可视化结果
        # VisualizePoseModelRseultsImage(InputImageRootPath, OutputImageRootPath, self.PoseModel, PoseResults, self.Dataset,
        #                                self.DatasetInfos, self.DemoConfigs)
        return PoseResults[0]["keypoints"]




if __name__ == "__main__":
    # 配置demoConfigsFilePath
    demoConfigsFilePath = "configs\demoConfigs.yaml".replace("\\", "/")
    # 获得键list和democonfigs dict
    demoConfigskeyList, demoConfigs = GetDemoConfigsFromConfigsFile(demoConfigsFilePath)
    print(f"\nDemo Configs Dict Keys List:{demoConfigskeyList}.", end="\n\n")

    # 加载模型
    startLoadModelTime = time.time()
    poseModel = init_pose_model(demoConfigs["POSE_CONFIGS_PATH"], demoConfigs["POSE_CHECKPOINT_PATH"],
                                demoConfigs["DEVICE"].lower())
    endLoadModelTime = time.time()
    LoadModelTime = endLoadModelTime - startLoadModelTime
    print(f"\nLoad Pose Model uses {LoadModelTime} second.", end="\n\n")

    # 加载数据处理的相关工具
    datasetType = poseModel.cfg.data["test"]["type"]
    print(f"datasetType:{datasetType}.", end="\n\n")
    datasetInfo = poseModel.cfg.data["test"].get("dataset_info", None)
    print(f"datasetInfo:{datasetInfo}.\n")
    datasetInfo = DatasetInfo(datasetInfo)
    print(f"datasetInfo:{datasetInfo}.\n")

    #
    return_heatmap = False
    output_layer_names = None

    poseResults, returnedOutputs = inference_top_down_pose_model(
        poseModel,
        demoConfigs["IMAGE_ROOT_PATH"].replace("\\", "/"),
        bbox_thr=None,
        person_results=None,
        dataset=datasetType,
        dataset_info=datasetInfo,
        return_heatmap=return_heatmap,
        outputs=output_layer_names
    )
    poseResults[0]["bbox"] = np.array([0, 0, 0, 0], dtype=np.int32)


    vis_pose_result(
        poseModel,
        demoConfigs["IMAGE_ROOT_PATH"].replace("\\", "/"),
        poseResults,
        dataset=datasetType,
        dataset_info=datasetInfo,
        kpt_score_thr=demoConfigs["KEYPOINT_SCORE_THRESHOLD"],
        radius=demoConfigs["REDIUS"],
        thickness=demoConfigs["THICKNESS"],
        show=False,
        out_file=demoConfigs["OUTPUT_IMAGE_ROOT_PATH"].replace("\\", "/"))
